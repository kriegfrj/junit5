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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
		var results = executeTestsForClass(TestCaseWithThreeInterceptors.class);

		results.tests().assertStatistics(stats -> stats.failed(0).succeeded(4));
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
	static class TestCaseWithThreeInterceptors {

		public TestCaseWithThreeInterceptors(TestReporter reporter) {
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
				publish(reporter, InvocationType.DYNAMIC_TEST);
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
		DYNAMIC_TEST,
		AFTER_EACH,
		AFTER_ALL
	}

	abstract static class ReportingInvocationInterceptor implements InvocationInterceptor {
		private final Class<TestCaseWithThreeInterceptors> testClass = TestCaseWithThreeInterceptors.class;
		private final String name;

		ReportingInvocationInterceptor(String name) {
			this.name = name;
		}

		@Override
		public void interceptBeforeAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).isEmpty();
			assertEquals(testClass.getDeclaredMethod("beforeAll", TestReporter.class), invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.BEFORE_ALL);
		}

		@Override
		public <T> T interceptTestClassConstructor(ReflectiveInvocation<T> invocation,
				ExtensionContext extensionContext) throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).isEmpty();
			assertEquals(testClass.getDeclaredConstructor(TestReporter.class), invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			return reportAndProceed(invocation, extensionContext, InvocationType.CONSTRUCTOR);
		}

		@Override
		public void interceptBeforeEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("beforeEach", TestReporter.class), invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.BEFORE_EACH);
		}

		@Override
		public void interceptTestMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("test", TestReporter.class), invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.TEST_METHOD);
		}

		@Override
		public void interceptTestTemplateMethod(ReflectiveInvocation<Void> invocation,
				ExtensionContext extensionContext) throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("testTemplate", Integer.TYPE, TestReporter.class),
				invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(2);
			assertThat(invocation.getArguments().get(0)).isInstanceOf(Integer.class);
			assertThat(invocation.getArguments().get(1)).isInstanceOf(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.TEST_TEMPLATE_METHOD);
		}

		@Override
		public <T> T interceptTestFactoryMethod(ReflectiveInvocation<T> invocation, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("testFactory", TestReporter.class), invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			return reportAndProceed(invocation, extensionContext, InvocationType.TEST_FACTORY_METHOD);
		}

		@Override
		public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			assertThat(extensionContext.getUniqueId()).isNotBlank();
			assertThat(extensionContext.getElement()).isEmpty();
			assertThat(extensionContext.getParent().flatMap(ExtensionContext::getTestMethod)).contains(
				testClass.getDeclaredMethod("testFactory", TestReporter.class));
			reportAndProceed(invocation, extensionContext, InvocationType.DYNAMIC_TEST);
		}

		@Override
		public void interceptAfterEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("afterEach", TestReporter.class), invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.AFTER_EACH);
		}

		@Override
		public void interceptAfterAllMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocation.getTargetClass());
			assertThat(invocation.getTarget()).isEmpty();
			assertEquals(testClass.getDeclaredMethod("afterAll", TestReporter.class), invocation.getExecutable());
			assertThat(invocation.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.AFTER_ALL);
		}

		private <T> T reportAndProceed(Invocation<T> invocation, ExtensionContext extensionContext, InvocationType type)
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
