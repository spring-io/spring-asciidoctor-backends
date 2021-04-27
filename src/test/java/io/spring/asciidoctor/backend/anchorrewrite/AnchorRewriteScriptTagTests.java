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

package io.spring.asciidoctor.backend.anchorrewrite;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Tests for {@link AnchorRewriteScriptTag}.
 *
 * @author Phillip Webb
 */
class AnchorRewriteScriptTagTests {

	private URL expected;

	@BeforeEach
	void setup(TestInfo testInfo) {
		String name = "AnchorRewriteScriptTagTests_" + testInfo.getTestMethod().get().getName() + ".html";
		this.expected = getClass().getResource(name);
	}

	@Test
	void getHtmlGeneratesScriptTag() {
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("foo", "bar");
		entries.put("green", "blue");
		AnchorRewriteScriptTag tag = new AnchorRewriteScriptTag(entries);
		assertThat(tag.getHtml()).isEqualTo(contentOf(this.expected));
	}

	@Test
	void getHtmlWhenHasEqualKeyValueEntryGeneratesFilteredHtml() {
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("foo", "foo");
		entries.put("green", "blue");
		AnchorRewriteScriptTag tag = new AnchorRewriteScriptTag(entries);
		assertThat(tag.getHtml()).isEqualTo(contentOf(this.expected));
	}

	@Test
	void getHtmlWhenEmptyGeneratesEmptyHtml() {
		Map<String, String> entries = new LinkedHashMap<>();
		AnchorRewriteScriptTag tag = new AnchorRewriteScriptTag(entries);
		assertThat(tag.getHtml()).isEqualTo("");
	}

	@Test
	void fromPropertiesFileLoadsFileContents() throws Exception {
		File file = new File(
				"src/test/java/" + getClass().getPackage().getName().replace(".", "/") + "/anchor-rewrite.properties");
		AnchorRewriteScriptTag tag = AnchorRewriteScriptTag.fromPropertiesFile(file);
		assertThat(tag.getHtml()).contains("foo");

	}

}
