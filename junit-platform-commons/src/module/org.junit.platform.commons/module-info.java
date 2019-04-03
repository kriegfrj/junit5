/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

module org.junit.platform.commons {
	requires java.compiler; // usage of `javax.lang.model.SourceVersion` in `PackageUtils`
	requires java.logging;
	requires static org.apiguardian.api;

	exports org.junit.platform.commons;
	exports org.junit.platform.commons.annotation;
	exports org.junit.platform.commons.support;

	exports org.junit.platform.commons.function to
			org.junit.platform.engine,
			org.junit.platform.console,
			org.junit.platform.launcher,
			org.junit.jupiter.engine,
			org.junit.vintage.engine;

	exports org.junit.platform.commons.logging to
			org.junit.platform.engine,
			org.junit.platform.console,
			org.junit.platform.launcher,
			org.junit.jupiter.engine,
			org.junit.vintage.engine;

	exports org.junit.platform.commons.util to
			org.junit.platform.engine,
			org.junit.platform.console,
			org.junit.platform.launcher,
			org.junit.jupiter.api,
			org.junit.jupiter.engine,
			org.junit.vintage.engine;
}
