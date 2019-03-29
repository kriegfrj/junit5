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

import java.lang.reflect.Executable;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.InvocationInterceptor.ReflectiveInvocation;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.ExceptionUtils;

public class InvocationInterceptorChain {

	public <T> T invoke(Invocation<T> originalInvocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<T, Invocation<T>> call) {
		return invoke(originalInvocation, extensionContext, extensionRegistry, call, InterceptedInvocation::new);
	}

	public <T> T invokeReflectively(ReflectiveInvocation<T> originalInvocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<T, ReflectiveInvocation<T>> call) {
		return invoke(originalInvocation, extensionContext, extensionRegistry, call,
			ReflectiveInterceptedInvocation::new);
	}

	private <R, T extends Invocation<R>> R invoke(T invocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<R, T> call, DecoratorFactory<R, T> decoratorFactory) {
		return proceed(decorateInvocation(invocation, extensionContext, extensionRegistry, call, decoratorFactory));
	}

	private <R, T extends Invocation<R>> T decorateInvocation(T invocation, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<R, T> call, DecoratorFactory<R, T> decoratorFactory) {
		T result = invocation;
		if (call != InterceptorCall.NONE) {
			List<InvocationInterceptor> interceptors = extensionRegistry.getExtensions(InvocationInterceptor.class);
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

		InterceptorCall<Object, Invocation<Object>> NONE = (interceptor, invocation,
				extensionContext) -> invocation.proceed();

		R execute(InvocationInterceptor interceptor, T invocation, ExtensionContext extensionContext) throws Throwable;

		static <T extends Invocation<Void>> InterceptorCall<Void, T> ofVoid(VoidInterceptorCall<T> call) {
			return ((interceptorChain, invocation, extensionContext) -> {
				call.execute(interceptorChain, invocation, extensionContext);
				return null;
			});
		}

		@SuppressWarnings("unchecked")
		static <R, T extends Invocation<R>> InterceptorCall<R, T> none() {
			return (InterceptorCall<R, T>) NONE;
		}

	}

	@FunctionalInterface
	public interface VoidInterceptorCall<T extends Invocation<Void>> {

		void execute(InvocationInterceptor interceptor, T invocation, ExtensionContext extensionContext)
				throws Throwable;

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
			return call.execute(interceptor, invocation, extensionContext);
		}

	}

	private static class ReflectiveInterceptedInvocation<T> extends InterceptedInvocation<T, ReflectiveInvocation<T>>
			implements ReflectiveInvocation<T> {

		ReflectiveInterceptedInvocation(ReflectiveInvocation<T> invocation,
				InterceptorCall<T, ReflectiveInvocation<T>> call, InvocationInterceptor interceptor,
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
