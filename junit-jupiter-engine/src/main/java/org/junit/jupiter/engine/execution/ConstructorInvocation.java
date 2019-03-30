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

import static java.util.Collections.unmodifiableList;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.InvocationInterceptor.ConstructorContext;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.platform.commons.util.ReflectionUtils;

class ConstructorInvocation<T> implements Invocation<T>, ConstructorContext {

	private final Constructor<? extends T> constructor;
	private final Object[] arguments;

	ConstructorInvocation(Constructor<? extends T> constructor, Object[] arguments) {
		this.constructor = constructor;
		this.arguments = arguments;
	}

	@Override
	public Class<?> getTargetClass() {
		return constructor.getDeclaringClass();
	}

	@Override
	public Constructor<?> getConstructor() {
		return constructor;
	}

	@Override
	public List<Object> getArguments() {
		return unmodifiableList(Arrays.asList(arguments));
	}

	@Override
	public T proceed() {
		return ReflectionUtils.newInstance(constructor, arguments);
	}
}
