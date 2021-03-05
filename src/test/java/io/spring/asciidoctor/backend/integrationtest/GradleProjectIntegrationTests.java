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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Maven.
 *
 * @author Phillip Webb
 */
public class GradleProjectIntegrationTests {

	@Test
	void gradleBuild() throws Exception {
		File projectDir = new File("src/test/gradle");
		Map<String, String> environment = new LinkedHashMap<>();
		environment.put("asciidoctor-spring-backends.version", ProjectVersion.get());
		environment.put("asciidoctor-spring-backends-repo",
				new File("build/maven-repository").getAbsoluteFile().toURI().toString());

		BuildResult result = GradleRunner.create().withProjectDir(projectDir).withEnvironment(environment)
				.forwardOutput().withArguments("clean", "asciidoctor").build();
		assertThat(result.task(":asciidoctor").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
		File generatedDocs = new File(projectDir, "build/docs/asciidoc");
		File htmlFile = new File(generatedDocs, "index.html");
		assertThat(new File(generatedDocs, "css/site.css")).exists();
		assertThat(new File(generatedDocs, "js/site.js")).exists();
		assertThat(htmlFile).exists();
		assertThat(new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8))
				.contains("<title>Gradle Example</title>").contains("main-container");
	}

}
