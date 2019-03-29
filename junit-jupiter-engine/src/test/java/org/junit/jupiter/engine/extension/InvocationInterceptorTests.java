/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

class InvocationInterceptorTests extends AbstractJupiterTestEngineTests {

	@Test
	void callsInterceptorsForTestMethod() {
		executeTestsForClass(TestCase.class);
		assertThat(TestCase.events) //
				.containsExactly("before:foo", "before:bar", "before:baz", "test", "after:baz", "after:bar",
					"after:foo");
	}

	@ExtendWith({ TestCase.FooInvocationInterceptor.class, TestCase.BarInvocationInterceptor.class,
			TestCase.BazInvocationInterceptor.class })
	static class TestCase {

		static final List<String> events = new ArrayList<>();

		@BeforeAll
		static void clearEvents() {
			events.clear();
		}

		@Test
		void test() {
			events.add("test");
		}

		abstract static class MyInvocationInterceptor implements InvocationInterceptor {
			private final String name;

			MyInvocationInterceptor(String name) {
				this.name = name;
			}

			@Override
			public void executeTestMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
					throws Throwable {
				events.add("before:" + name);
				try {
					invocation.proceed();
				}
				finally {
					events.add("after:" + name);
				}
			}
		}

		static class FooInvocationInterceptor extends MyInvocationInterceptor {
			FooInvocationInterceptor() {
				super("foo");
			}
		}

		static class BarInvocationInterceptor extends MyInvocationInterceptor {
			BarInvocationInterceptor() {
				super("bar");
			}
		}

		static class BazInvocationInterceptor extends MyInvocationInterceptor {
			BazInvocationInterceptor() {
				super("baz");
			}
		}
	}

}
