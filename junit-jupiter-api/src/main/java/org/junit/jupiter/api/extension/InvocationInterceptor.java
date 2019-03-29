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

	default <T> T interceptTestClassConstructor(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext)
			throws Throwable {
		return invocation.proceed();
	}

	default void interceptBeforeAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void interceptBeforeEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void interceptTestMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default <T> T interceptTestFactoryMethod(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext)
			throws Throwable {
		return invocation.proceed();
	}

	default void interceptTestTemplateMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	default void interceptAfterEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		invocation.proceed();
	}

	default void interceptAfterAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
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
