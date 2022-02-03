/*
 * Copyright 2021-2022 the original author or authors.
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

package io.spring.asciidoctor.backend.codetools;

import io.spring.asciidoctor.backend.testsupport.AsciidoctorExtension;
import io.spring.asciidoctor.backend.testsupport.ConvertedHtml;
import io.spring.asciidoctor.backend.testsupport.ExpectedHtml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ChompListingContentConverter}.
 *
 * @author Phillip Webb
 */
@ExtendWith(AsciidoctorExtension.class)
class ChompListingContentConverterTests {

	@Test
	void convertWhenNotSupportedLanguageReturnsContent(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("&lt;!-- /**/ test --&gt;");
	}

	@Test
	void convertWhenChompHeaderReturnsWithoutHeader(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("package com.example;");
	}

	@Test
	void convertWhenChompHeaderAndKotlinReturnsWithoutHeader(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("package com.example");
	}

	@Test
	void convertWhenChompPackageReturnsWithoutPackage(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("public class Example {}");
	}

	@Test
	void convertWhenChompPackageAndKotlinReturnsWithoutPackage(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("public class Example {}");
	}

	@Test
	void convertWhenChompPackageAndGroovyReturnsWithoutPackage(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("public class Example {}");
	}

	@Test
	void convertWhenChompPackageAndReplacementReturnsWithReplacedPackage(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByTag("code")).satisfies(expected);
	}

	@Test
	void convertWhenShortChompTagReturnsChompedLines(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("Object chomp = ...");
	}

	@Test
	void convertWhenMultiLineShortChompTagsReturnsChompedLines(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByTag("code")).satisfies(expected);
	}

	@Test
	void convertWhenNoChompsReturnsContent(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("Object nonChomp = /* comment */ new Object();");
	}

	@Test
	void convertWhenLineChompTagReturnsChompedLines(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("Object o =");
	}

	@Test
	void convertWhenLineChompTagWithReplacementReturnsChompedLines(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("Object o = // ... your instance");
	}

	@Test
	void convertWhenFileChompTagReturnsChompedLines(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("public class Example {}");
	}

	@Test
	void convertWhenFormattersChompReturnsChompedLines(ConvertedHtml html) {
		assertThat(html.getElementByTag("code")).isEqualTo("public class Example {}");
	}

	@Test
	void convertWhenMethodFormattersChompReturnsChompedLines(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByTag("code")).satisfies(expected);
	}

	@Test
	void convertWhenSuppressWarningsChompReturnsChompedLines(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByTag("code")).satisfies(expected);
	}

	@Test
	void convertWhenMultipleChompsReturnsChompedLines(ConvertedHtml html, ExpectedHtml expected) {
		assertThat(html.getElementByTag("code")).satisfies(expected);
	}

}
