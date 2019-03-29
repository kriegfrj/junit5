/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.Optional;

public interface InvocationInterceptor extends Extension {

	default <T> T executeTestClassConstructor(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext)
			throws Throwable {
		return invocation.proceed();
	}

	default void executeBeforeAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void executeBeforeEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void executeTestMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default <T> T executeTestFactoryMethod(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext)
			throws Throwable {
		return invocation.proceed();
	}

	default void executeTestTemplateMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void executeDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	default void executeAfterEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void executeAfterAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	interface Invocation<T> {
		T proceed() throws Throwable;
	}

	interface ReflectiveInvocation<T> extends Invocation<T> {
		Class<?> getTargetClass();

		Optional<Object> getTarget(); // empty for static methods

		Executable getExecutable();

		List<Object> getArguments();
	}

}
