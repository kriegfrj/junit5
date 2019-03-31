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

import static java.util.stream.Collectors.joining;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;

@API(status = INTERNAL, since = "5.5")
public class InvocationInterceptorChain {

	public <C, T> T invoke(Invocation<T> invocation, C invocationContext, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, InterceptorCall<C, T> call) {
		List<InvocationInterceptor> interceptors = extensionRegistry.getExtensions(InvocationInterceptor.class);
		if (interceptors.isEmpty()) {
			return proceed(invocation);
		}
		return chainAndInvoke(invocation, invocationContext, extensionContext, call, interceptors);
	}

	private <C, T> T chainAndInvoke(Invocation<T> invocation, C invocationContext, ExtensionContext extensionContext,
			InterceptorCall<C, T> call, List<InvocationInterceptor> interceptors) {
		ValidatingInvocation<T> validatingInvocation = new ValidatingInvocation<>(invocation, interceptors);
		Invocation<T> chainedInvocation = chainInterceptors(validatingInvocation, invocationContext, interceptors, call,
			extensionContext);
		T result = proceed(chainedInvocation);
		validatingInvocation.verifyInvokedAtLeastOnce();
		return result;
	}

	private <C, T> Invocation<T> chainInterceptors(Invocation<T> invocation, C invocationContext,
			List<InvocationInterceptor> interceptors, InterceptorCall<C, T> call, ExtensionContext extensionContext) {
		Invocation<T> result = invocation;
		ListIterator<InvocationInterceptor> iterator = interceptors.listIterator(interceptors.size());
		while (iterator.hasPrevious()) {
			InvocationInterceptor interceptor = iterator.previous();
			result = new InterceptedInvocation<>(result, invocationContext, call, interceptor, extensionContext);
		}
		return result;
	}

	private <T> T proceed(Invocation<T> invocation) {
		try {
			return invocation.proceed();
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(t);
		}
	}

	@FunctionalInterface
	public interface InterceptorCall<C, T> {

		T apply(InvocationInterceptor interceptor, Invocation<T> invocation, C invocationContext,
				ExtensionContext extensionContext) throws Throwable;

		static <C> InterceptorCall<C, Void> ofVoid(VoidInterceptorCall<C> call) {
			return ((interceptorChain, invocation, invocationContext, extensionContext) -> {
				call.apply(interceptorChain, invocation, invocationContext, extensionContext);
				return null;
			});
		}

	}

	@FunctionalInterface
	public interface VoidInterceptorCall<C> {

		void apply(InvocationInterceptor interceptor, Invocation<Void> invocation, C invocationContext,
				ExtensionContext extensionContext) throws Throwable;

	}

	private static class InterceptedInvocation<C, T> implements Invocation<T> {

		private final Invocation<T> invocation;
		private final C invocationContext;
		private final InterceptorCall<C, T> call;
		private final InvocationInterceptor interceptor;
		private final ExtensionContext extensionContext;

		InterceptedInvocation(Invocation<T> invocation, C invocationContext, InterceptorCall<C, T> call,
				InvocationInterceptor interceptor, ExtensionContext extensionContext) {
			this.invocation = invocation;
			this.invocationContext = invocationContext;
			this.call = call;
			this.interceptor = interceptor;
			this.extensionContext = extensionContext;
		}

		@Override
		public T proceed() throws Throwable {
			return call.apply(interceptor, invocation, invocationContext, extensionContext);
		}

	}

	private static class ValidatingInvocation<T> implements Invocation<T> {

		private final AtomicBoolean invoked = new AtomicBoolean();
		private final Invocation<T> delegate;
		private final List<InvocationInterceptor> interceptors;

		ValidatingInvocation(Invocation<T> delegate, List<InvocationInterceptor> interceptors) {
			this.delegate = delegate;
			this.interceptors = interceptors;
		}

		@Override
		public T proceed() throws Throwable {
			if (!invoked.compareAndSet(false, true)) {
				fail("Chain of InvocationInterceptors called invocation multiple times instead of just once");
			}
			return delegate.proceed();
		}

		void verifyInvokedAtLeastOnce() {
			if (!invoked.get()) {
				fail("Chain of InvocationInterceptors never called invocation");
			}
		}

		private void fail(String prefix) {
			String commaSeparatedInterceptorClasses = interceptors.stream().map(Object::getClass).map(
				Class::getName).collect(joining(", "));
			throw new JUnitException(prefix + ": " + commaSeparatedInterceptorClasses);
		}

	}

}
