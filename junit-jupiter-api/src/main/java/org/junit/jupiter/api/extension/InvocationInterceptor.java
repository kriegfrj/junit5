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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;

/**
 * {@code InvocationInterceptor} defines the API for {@link Extension
 * Extensions} that wish to intercept calls to test code.
 *
 * <h3>Invocation Contract</h3>
 *
 * <p>Each method in this class must execute the passed {@link Invocation}
 * exactly once. Otherwise, the enclosing test or container will be reported as
 * failed.
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.5
 * @see Invocation
 * @see ConstructorContext
 * @see MethodContext
 * @see ExtensionContext
 */
@API(status = EXPERIMENTAL, since = "5.5")
public interface InvocationInterceptor extends Extension {

	/**
	 * Intercept the invocation of a test class constructor.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @param <T> the result type
	 * @return the result of the invocation; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default <T> T interceptTestClassConstructor(Invocation<T> invocation, ConstructorContext constructorContext,
			ExtensionContext extensionContext) throws Throwable {
		return invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link BeforeAll @BeforeAll} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptBeforeAllMethod(Invocation<Void> invocation, MethodContext methodContext,
			ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link BeforeEach @BeforeEach} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptBeforeEachMethod(Invocation<Void> invocation, MethodContext methodContext,
			ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link Test @Test} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptTestMethod(Invocation<Void> invocation, MethodContext methodContext,
			ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link TestFactory @TestFactory} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @param <T> the result type
	 * @return the result of the invocation; potentially {@code null}
	 * @throws Throwable in case of failures
	 */
	default <T> T interceptTestFactoryMethod(Invocation<T> invocation, MethodContext methodContext,
			ExtensionContext extensionContext) throws Throwable {
		return invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link TestTemplate @TestTemplate} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptTestTemplateMethod(Invocation<Void> invocation, MethodContext methodContext,
			ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link DynamicTest}.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of an {@link AfterEach @AfterEach} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptAfterEachMethod(Invocation<Void> invocation, MethodContext methodContext,
			ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of an {@link AfterAll @AfterAll} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptAfterAllMethod(Invocation<Void> invocation, MethodContext methodContext,
			ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * An invocation that returns a result and may throw a {@link Throwable}.
	 *
	 * <p>This interface is not intended to be implemented by clients.
	 *
	 * @param <T> the result type
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	interface Invocation<T> {

		/**
		 * Proceed with this invocation.
		 *
		 * @return the result of this invocation; potentially {@code null}.
		 * @throws Throwable in case the invocation failed
		 */
		T proceed() throws Throwable;

	}

	/**
	 * The context of an executable (method or constructor) to be invoked via
	 * reflection.
	 *
	 * <p>This interface is not intended to be implemented by clients.
	 *
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	interface ExecutableContext {

		/**
		 * Get the target class of this context.
		 *
		 * <p>If this invocation represents an instance method, this method
		 * returns the class of the object the method will be invoked on, not
		 * the class it is declared in. Otherwise, i.e. if this invocation
		 * represents a static method or constructor, this method returns the
		 * class the method or constructor is declared in.
		 *
		 * @return the target class of this invocation; never {@code null}
		 */
		Class<?> getTargetClass();

		/**
		 * Get the method or constructor of this context.
		 *
		 * <p>If this invocation represents a method, this method returns an
		 * instance of {@link Method}. Otherwise, i.e. if this invocation
		 * represents a constructor, this method returns an instance of
		 * {@link Constructor}.
		 *
		 * @return the executable of this context; never {@code null}
		 */
		Executable getExecutable();

		/**
		 * Get the arguments of the executable in this context.
		 *
		 * @return the arguments of the executable in this context; never
		 * {@code null}
		 */
		List<Object> getArguments();

	}

	/**
	 * The context of a constructor to be invoked via reflection.
	 *
	 * <p>This interface is not intended to be implemented by clients.
	 *
	 * @since 5.5
	 */
	interface ConstructorContext extends ExecutableContext {

		/**
		 * Get the constructor of this context.
		 *
		 * @return the constructor of this context; never {@code null}
		 */
		@Override
		default Constructor<?> getExecutable() {
			return getConstructor();
		}

		/**
		 * Get the constructor of this context.
		 *
		 * <p>This method is simply an alias for {@link #getExecutable()}.
		 *
		 * @return the constructor of this context; never {@code null}
		 */
		Constructor<?> getConstructor();

	}

	/**
	 * The context of a method to be invoked via reflection.
	 *
	 * <p>This interface is not intended to be implemented by clients.
	 *
	 * @since 5.5
	 */
	interface MethodContext extends ExecutableContext {

		/**
		 * Get the method of this context.
		 *
		 * @return the method of this context; never {@code null}
		 */
		@Override
		default Method getExecutable() {
			return getMethod();
		}

		/**
		 * Get the method of this context.
		 *
		 * <p>This method is simply an alias for {@link #getExecutable()}.
		 *
		 * @return the method of this context; never {@code null}
		 */
		Method getMethod();

		/**
		 * Get the target object of this reflective invocation, if available.
		 *
		 * <p>If this invocation represents an instance method, this method
		 * returns the object the method will be invoked on. Otherwise, i.e. if
		 * this invocation represents a static method, this method returns
		 * {@link Optional#empty() empty()}.
		 *
		 * @return the target of the method of this context; never {@code null}
		 * but potentially empty
		 */
		Optional<Object> getTarget();

	}

}
