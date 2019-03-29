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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.EnumSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;

class InvocationInterceptorTests extends AbstractJupiterTestEngineTests {

	@ParameterizedTest
	@EnumSource(InvocationType.class)
	void callsInterceptors(InvocationType invocationType) {
		var results = executeTestsForClass(TestMethodWithThreeInterceptors.class);

		assertThat(getEvents(results, EnumSet.of(invocationType)).distinct()) //
				.containsExactly("before:foo", "before:bar", "before:baz", "test", "after:baz", "after:bar",
					"after:foo");
	}

	private Stream<String> getEvents(EngineExecutionResults results, EnumSet<InvocationType> types) {
		return results.all().reportingEntryPublished() //
				.map(event -> event.getPayload(ReportEntry.class).orElseThrow()) //
				.map(ReportEntry::getKeyValuePairs) //
				.filter(map -> map.keySet().stream().map(InvocationType::valueOf).anyMatch(types::contains)) //
				.flatMap(map -> map.values().stream());
	}

	@ExtendWith({ FooInvocationInterceptor.class, BarInvocationInterceptor.class, BazInvocationInterceptor.class })
	static class TestMethodWithThreeInterceptors {

		public TestMethodWithThreeInterceptors(TestReporter reporter) {
			publish(reporter, InvocationType.CONSTRUCTOR);
		}

		@BeforeAll
		static void beforeAll(TestReporter reporter) {
			publish(reporter, InvocationType.BEFORE_ALL);
		}

		@BeforeEach
		void beforeEach(TestReporter reporter) {
			publish(reporter, InvocationType.BEFORE_EACH);
		}

		@Test
		void test(TestReporter reporter) {
			publish(reporter, InvocationType.TEST_METHOD);
		}

		@ParameterizedTest
		@ValueSource(ints = { 0, 1 })
		void testTemplate(int i, TestReporter reporter) {
			publish(reporter, InvocationType.TEST_TEMPLATE_METHOD);
		}

		@TestFactory
		DynamicTest testFactory(TestReporter reporter) {
			publish(reporter, InvocationType.TEST_FACTORY_METHOD);
			return dynamicTest("dynamicTest", () -> {
			});
		}

		@AfterEach
		void afterEach(TestReporter reporter) {
			publish(reporter, InvocationType.AFTER_EACH);
		}

		@AfterAll
		static void afterAll(TestReporter reporter) {
			publish(reporter, InvocationType.AFTER_ALL);
		}

		static void publish(TestReporter reporter, InvocationType type) {
			reporter.publishEntry(type.name(), "test");
		}

	}

	enum InvocationType {
		BEFORE_ALL,
		CONSTRUCTOR,
		BEFORE_EACH,
		TEST_METHOD,
		TEST_TEMPLATE_METHOD,
		TEST_FACTORY_METHOD,
		AFTER_EACH,
		AFTER_ALL
	}

	abstract static class ReportingInvocationInterceptor implements InvocationInterceptor {
		private final String name;

		ReportingInvocationInterceptor(String name) {
			this.name = name;
		}

		@Override
		public void executeBeforeAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			wrap(invocation, extensionContext, InvocationType.BEFORE_ALL);
		}

		@Override
		public <T> T executeTestClassConstructor(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext)
				throws Throwable {
			return wrap(invocation, extensionContext, InvocationType.CONSTRUCTOR);
		}

		@Override
		public void executeBeforeEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			wrap(invocation, extensionContext, InvocationType.BEFORE_EACH);
		}

		@Override
		public void executeTestMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			wrap(invocation, extensionContext, InvocationType.TEST_METHOD);
		}

		@Override
		public void executeTestTemplateMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			wrap(invocation, extensionContext, InvocationType.TEST_TEMPLATE_METHOD);
		}

		@Override
		public <T> T executeTestFactoryMethod(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext)
				throws Throwable {
			return wrap(invocation, extensionContext, InvocationType.TEST_FACTORY_METHOD);
		}

		@Override
		public void executeAfterEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			wrap(invocation, extensionContext, InvocationType.AFTER_EACH);
		}

		@Override
		public void executeAfterAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			wrap(invocation, extensionContext, InvocationType.AFTER_ALL);
		}

		private <T> T wrap(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext, InvocationType type)
				throws Throwable {
			extensionContext.publishReportEntry(type.name(), "before:" + name);
			try {
				return invocation.proceed();
			}
			finally {
				extensionContext.publishReportEntry(type.name(), "after:" + name);
			}
		}
	}

	static class FooInvocationInterceptor extends ReportingInvocationInterceptor {
		FooInvocationInterceptor() {
			super("foo");
		}
	}

	static class BarInvocationInterceptor extends ReportingInvocationInterceptor {
		BarInvocationInterceptor() {
			super("bar");
		}
	}

	static class BazInvocationInterceptor extends ReportingInvocationInterceptor {
		BazInvocationInterceptor() {
			super("baz");
		}
	}

}
