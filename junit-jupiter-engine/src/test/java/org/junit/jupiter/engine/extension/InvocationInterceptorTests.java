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

import java.util.EnumSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;

class InvocationInterceptorTests extends AbstractJupiterTestEngineTests {

	@ParameterizedTest
	@EnumSource(InvocationType.class)
	void callsInterceptorsForBeforeEachMethod(InvocationType invocationType) {
		var results = executeTestsForClass(TestCaseWithThreeInterceptors.class);

		assertThat(getEvents(results, EnumSet.of(invocationType))) //
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

		@BeforeEach
		void beforeEach(TestReporter reporter) {
			publish(reporter, InvocationType.BEFORE_EACH);
		}

		@Test
		void test(TestReporter reporter) {
			publish(reporter, InvocationType.TEST_METHOD);
		}

		@AfterEach
		void afterEach(TestReporter reporter) {
			publish(reporter, InvocationType.AFTER_EACH);
		}

		private void publish(TestReporter reporter, InvocationType type) {
			reporter.publishEntry(type.name(), "test");
		}
	}

	enum InvocationType {
		BEFORE_EACH, TEST_METHOD, AFTER_EACH
	}

	abstract static class ReportingInvocationInterceptor implements InvocationInterceptor {
		private final String name;

		ReportingInvocationInterceptor(String name) {
			this.name = name;
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
		public void executeAfterEachMethod(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext)
				throws Throwable {
			wrap(invocation, extensionContext, InvocationType.AFTER_EACH);
		}

		private void wrap(ReflectiveInvocation<Void> invocation, ExtensionContext extensionContext, InvocationType type)
				throws Throwable {
			extensionContext.publishReportEntry(type.name(), "before:" + name);
			try {
				invocation.proceed();
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
