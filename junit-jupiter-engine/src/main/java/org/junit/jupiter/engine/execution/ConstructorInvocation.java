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
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.platform.commons.util.ReflectionUtils;

class ConstructorInvocation<T> implements InvocationInterceptor.ReflectiveInvocation<T> {

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
	public Optional<Object> getTarget() {
		return Optional.empty();
	}

	@Override
	public Executable getExecutable() {
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
