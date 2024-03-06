/*
 * Copyright 2021-2024 the original author or authors.
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

package io.spring.asciidoctor.backend.includecode;

import io.spring.asciidoctor.backend.testsupport.AsciidoctorExtension;
import io.spring.asciidoctor.backend.testsupport.ConvertedHtml;
import io.spring.asciidoctor.backend.testsupport.ConvertedHtmlSupplier;
import io.spring.asciidoctor.backend.testsupport.ExpectedHtml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link IncludeCodeIncludeProcessor}.
 *
 * @author Phillip Webb
 * @author Sebastien Deleuze
 */
@ExtendWith(AsciidoctorExtension.class)
class IncludeCodeIncludeProcessorTests {

	@Test
	void includeOnlyJava(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByClass("sectionbody")).satisfies(expected.whenIgnoringTrailingWhitespace());
	}

	@Test
	void includeOnlyJavaWithTag(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByClass("sectionbody")).satisfies(expected.whenIgnoringTrailingWhitespace());
	}

	@Test
	void includeJavaAndKotlin(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByClass("sectionbody")).satisfies(expected.whenIgnoringTrailingWhitespace());
	}

	@Test
	void includeJavaKotlinAndXml(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByClass("sectionbody")).satisfies(expected.whenIgnoringTrailingWhitespace());
	}

	@Test
	void includeJavaAndKotlinWithMissingJavaFile(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByClass("sectionbody")).satisfies(expected.whenIgnoringTrailingWhitespace());
	}

	@Test
	void includeMissingAllCode(ConvertedHtmlSupplier html) {
		assertThatIllegalStateException().isThrownBy(() -> html.get()).withStackTraceContaining("Unable to find code");
	}

	@Test
	void includeWithPackageReplacementChomp(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).contains("package com.example;");
	}

}
