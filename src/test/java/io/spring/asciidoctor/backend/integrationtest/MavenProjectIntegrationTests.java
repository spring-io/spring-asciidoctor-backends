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
import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Maven.
 *
 * @author Phillip Webb
 */
public class MavenProjectIntegrationTests {

	private final File baseDirectory = new File("src/test/maven");

	@Test
	void mavenBuild() throws Exception {
		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(getMavenHome());
		invoker.setOutputHandler(System.out::println);
		invoker.setErrorHandler(System.err::println);
		InvocationRequest request = createRequest();
		InvocationResult result = invoker.execute(request);
		File generatedDocs = new File(this.baseDirectory, "target/generated-docs");
		File htmlFile = new File(generatedDocs, "index.html");
		assertThat(result.getExitCode()).isEqualTo(0);
		assertThat(new File(generatedDocs, "css/site.css")).exists();
		assertThat(new File(generatedDocs, "js/site.js")).exists();
		assertThat(htmlFile).exists();
		assertThat(new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8))
				.contains("<title>Maven Example</title>").contains("main-container").contains("new-anchor");
		File pdfFile = new File(generatedDocs, "index.pdf");
		assertThat(pdfFile).exists();
	}

	private InvocationRequest createRequest() throws Exception {
		InvocationRequest request = new DefaultInvocationRequest();
		Properties systemProperties = new Properties();
		systemProperties.setProperty("maven.localrepo", new File("build/maven-local-repository").getAbsolutePath());
		systemProperties.setProperty("spring-asciidoctor-backends.version", ProjectVersion.get());
		systemProperties.setProperty("spring-asciidoctor-backends-repo",
				new File("build/maven-repository").getAbsoluteFile().toURI().toString());
		request.setProperties(systemProperties);
		request.setBaseDirectory(this.baseDirectory);
		request.setUserSettingsFile(new File(this.baseDirectory, "settings.xml"));
		request.setGoals(Arrays.asList("clean", "package"));
		request.setUpdateSnapshots(true);
		return request;
	}

	private File getMavenHome() {
		File parent = new File("build/maven-binary");
		File[] files = parent.listFiles(this::isMavenBinaryFolder);
		assertThat(files).hasSize(1);
		return files[0];
	}

	private boolean isMavenBinaryFolder(File file) {
		return file.getName().startsWith("apache-maven");
	}

}
