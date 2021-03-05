/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.asciidoctor.backend.integrationtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides access to the version that we're building.
 *
 * @author Phillip Webb
 */
final class ProjectVersion {

	private ProjectVersion() {
	}

	static String get() throws IOException {
		try (FileInputStream inputStream = new FileInputStream(new File("gradle.properties"))) {
			Properties properties = new Properties();
			properties.load(inputStream);
			return properties.getProperty("version");
		}
	}

}
