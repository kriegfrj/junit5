/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.InvocationInterceptor.ReflectiveInvocation;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.ExceptionUtils;

@API(status = INTERNAL, since = "5.5")
public class InvocationInterceptorChain {

	public <T> T invoke(Invocation<T> invocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<T, Invocation<T>> call) {
		return invoke(invocation, extensionContext, extensionRegistry, call, InterceptedInvocation::new);
	}

	<T> T invokeReflectively(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<T, ReflectiveInvocation<T>> call) {
		return invoke(invocation, extensionContext, extensionRegistry, call, InterceptedReflectiveInvocation::new);
	}

	private <R, T extends Invocation<R>> R invoke(T invocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<R, T> call, DecoratorFactory<R, T> decoratorFactory) {
		return proceed(decorateInvocation(invocation, extensionContext, extensionRegistry, call, decoratorFactory));
	}

	private <R, T extends Invocation<R>> T decorateInvocation(T invocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<R, T> call, DecoratorFactory<R, T> decoratorFactory) {
		T result = invocation;
		List<InvocationInterceptor> interceptors = extensionRegistry.getExtensions(InvocationInterceptor.class);
		if (!interceptors.isEmpty()) {
			ListIterator<InvocationInterceptor> iterator = interceptors.listIterator(interceptors.size());
			while (iterator.hasPrevious()) {
				InvocationInterceptor interceptor = iterator.previous();
				result = decoratorFactory.decorate(result, call, interceptor, extensionContext);
			}
		}
		return result;
	}

	private <R> R proceed(Invocation<R> invocation) {
		try {
			return invocation.proceed();
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(t);
		}
	}

	@FunctionalInterface
	private interface DecoratorFactory<R, T extends Invocation<R>> {

		T decorate(T invocation, InterceptorCall<R, T> call, InvocationInterceptor interceptor,
				ExtensionContext extensionContext);

	}

	@FunctionalInterface
	public interface InterceptorCall<R, T extends Invocation<R>> {

		R apply(InvocationInterceptor interceptor, T invocation, ExtensionContext extensionContext) throws Throwable;

		static <T extends Invocation<Void>> InterceptorCall<Void, T> ofVoid(VoidInterceptorCall<T> call) {
			return ((interceptorChain, invocation, extensionContext) -> {
				call.apply(interceptorChain, invocation, extensionContext);
				return null;
			});
		}

	}

	@FunctionalInterface
	public interface VoidInterceptorCall<T extends Invocation<Void>> {

		void apply(InvocationInterceptor interceptor, T invocation, ExtensionContext extensionContext) throws Throwable;

	}

	private static class InterceptedInvocation<R, T extends Invocation<R>> implements Invocation<R> {

		protected final T invocation;
		private final InterceptorCall<R, T> call;
		private final InvocationInterceptor interceptor;
		private final ExtensionContext extensionContext;

		InterceptedInvocation(T invocation, InterceptorCall<R, T> call, InvocationInterceptor interceptor,
				ExtensionContext extensionContext) {
			this.invocation = invocation;
			this.call = call;
			this.interceptor = interceptor;
			this.extensionContext = extensionContext;
		}

		@Override
		public R proceed() throws Throwable {
			return call.apply(interceptor, invocation, extensionContext);
		}

	}

	private static class InterceptedReflectiveInvocation<R> extends InterceptedInvocation<R, ReflectiveInvocation<R>>
			implements ReflectiveInvocation<R> {

		InterceptedReflectiveInvocation(ReflectiveInvocation<R> invocation,
				InterceptorCall<R, ReflectiveInvocation<R>> call, InvocationInterceptor interceptor,
				ExtensionContext extensionContext) {
			super(invocation, call, interceptor, extensionContext);
		}

		@Override
		public Class<?> getTargetClass() {
			return invocation.getTargetClass();
		}

		@Override
		public Optional<Object> getTarget() {
			return invocation.getTarget();
		}

		@Override
		public Executable getExecutable() {
			return invocation.getExecutable();
		}

		@Override
		public List<Object> getArguments() {
			return invocation.getArguments();
		}

	}

}
