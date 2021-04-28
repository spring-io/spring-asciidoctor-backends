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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Generates the script tag for anchor rewrites.
 *
 * @author Phillip Webb
 */
class AnchorRewriteScriptTag {

	private final Map<String, String> entries;

	AnchorRewriteScriptTag(Map<?, ?> entries) {
		this.entries = getValidEntries(entries);
	}

	private Map<String, String> getValidEntries(Map<?, ?> entries) {
		Map<String, String> validEntries = new TreeMap<>();
		entries.forEach((key, value) -> {
			if (!Objects.equals(key, value)) {
				validEntries.put(String.valueOf(key), String.valueOf(value));
			}
		});
		return Collections.unmodifiableMap(validEntries);
	}

	String getHtml() {
		if (this.entries.isEmpty()) {
			return "";
		}
		StringBuilder html = new StringBuilder();
		html.append("<script type=\"application/json\" id=\"anchor-rewrite\">\n");
		html.append("{\n");
		Iterator<Entry<String, String>> iterator = this.entries.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			html.append(jsonQuote(entry.getKey()));
			html.append(":");
			html.append(jsonQuote(entry.getValue()));
			html.append(iterator.hasNext() ? ",\n" : "\n");
		}
		html.append("}\n");
		html.append("</script>");
		return html.toString();
	}

	private String jsonQuote(String string) {
		return "\"" + string.replace("\"", "\\\"") + "\"";
	}

	static AnchorRewriteScriptTag fromPropertiesFile(File propertiesFile) throws IOException {
		Properties properties = new Properties();
		try (InputStream inputStream = new FileInputStream(propertiesFile)) {
			properties.load(inputStream);
		}
		return new AnchorRewriteScriptTag(properties);
	}

}
