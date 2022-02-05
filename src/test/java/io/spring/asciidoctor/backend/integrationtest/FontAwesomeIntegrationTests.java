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
import io.spring.asciidoctor.backend.testsupport.ConvertedHtml;
import io.spring.asciidoctor.backend.testsupport.Source;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for tab support.
 *
 * @author Phillip Webb
 */
@ExtendWith(AsciidoctorExtension.class)
public class FontAwesomeIntegrationTests {

	private static final String CSS_LINK = "<link rel=\"stylesheet\" href=\"css/font-awesome.css\">";

	private static final String ICON_TEXT = "<i class=\"fa fa-folder\"></i>";

	@Test
	void fontAwesomeCssIsIncludedWhenAttributeIsSet(@Source("FontAwesome.adoc") ConvertedHtml html) {
		assertThat(html.toString()).contains(CSS_LINK);
		assertThat(html.toString()).contains(ICON_TEXT);
	}

	@Test
	void fontAwesomeCssIsNotIncludedByDefault(@Source("NoFontAwesome.adoc") ConvertedHtml html) {
		assertThat(html.toString()).doesNotContain(CSS_LINK);
		assertThat(html.toString()).contains(ICON_TEXT);
	}

}
