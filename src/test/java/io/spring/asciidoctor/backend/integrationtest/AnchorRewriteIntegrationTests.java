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

import io.spring.asciidoctor.backend.testsupport.AsciidoctorExtension;
import io.spring.asciidoctor.backend.testsupport.ChromeContainer;
import io.spring.asciidoctor.backend.testsupport.ConvertedHtml;
import io.spring.asciidoctor.backend.testsupport.Source;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for tab support.
 *
 * @author Phillip Webb
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(AsciidoctorExtension.class)
public class AnchorRewriteIntegrationTests {

	@Container
	private final ChromeContainer chrome = new ChromeContainer();

	@Test
	void whenNotInFileAnchorIsUnchanged(@Source("anchors.adoc") ConvertedHtml html) {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome, "two");
		assertThat(driver.getCurrentUrl()).endsWith("#two");
	}

	@Test
	void whenInFileAnchorIsRewritten(@Source("anchors.adoc") ConvertedHtml html) {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome, "oldone");
		assertThat(driver.getCurrentUrl()).endsWith("#one");
	}

	@Test
	void whenLoopLogsAndLeaves(@Source("anchors.adoc") ConvertedHtml html) {
		RemoteWebDriver driver = html.inWebBrowser(this.chrome, "loop", (logs) -> {
			assertThat(logs).hasSize(1);
			assertThat(logs.iterator().next().getMessage()).endsWith("\"Skipping circular anchor update\"");
		});
		assertThat(driver.getCurrentUrl()).endsWith("#loop");
	}

}
