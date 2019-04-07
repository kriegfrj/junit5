/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.InvocationInterceptorChain;
import org.junit.jupiter.engine.execution.InvocationInterceptorChain.InterceptorCall;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for a {@link DynamicTest}.
 *
 * @since 5.0
 */
class DynamicTestTestDescriptor extends DynamicNodeTestDescriptor {

	private static final InvocationInterceptorChain interceptorChain = new InvocationInterceptorChain();
	private static final InterceptorCall<Void, Void> INTERCEPTOR_CALL = InterceptorCall.ofVoid((interceptor, invocation,
			invocationContext, extensionContext) -> interceptor.interceptDynamicTest(invocation, extensionContext));
	private final DynamicTest dynamicTest;

	DynamicTestTestDescriptor(UniqueId uniqueId, int index, DynamicTest dynamicTest, TestSource source,
			JupiterConfiguration configuration) {
		super(uniqueId, index, dynamicTest, source, configuration);
		this.dynamicTest = dynamicTest;
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) {
		InvocationInterceptor.Invocation<Void> invocation = () -> {
			dynamicTest.getExecutable().execute();
			return null;
		};
		ExtensionContext extensionContext = context.getExtensionContext();
		ExtensionRegistry extensionRegistry = context.getExtensionRegistry();
		interceptorChain.invoke(invocation, null, extensionContext, extensionRegistry, INTERCEPTOR_CALL);
		return context;
	}

}
