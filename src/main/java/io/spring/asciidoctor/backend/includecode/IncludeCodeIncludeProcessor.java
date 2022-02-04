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

package io.spring.asciidoctor.backend.includecode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.spring.asciidoctor.backend.language.Language;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;

/**
 * {@link IncludeProcessor} to deal with convention based code imports.
 *
 * @author Phillip Webb
 */
public class IncludeCodeIncludeProcessor extends IncludeProcessor {

	private static final String PREFIX = "code:";

	private final Map<String, AsciidoctorFile> files = new LinkedHashMap<String, AsciidoctorFile>() {

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, AsciidoctorFile> eldest) {
			return size() > 10;
		};

	};

	@Override
	public boolean handles(String target) {
		return target.startsWith(PREFIX);
	}

	@Override
	public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
		target = target.substring(PREFIX.length());
		String includeAttributes = getIncludeAttributes(attributes);
		AsciidoctorFile asciidoctorFile = this.files.computeIfAbsent(reader.getFile(), AsciidoctorFile::new);
		String sectionId = asciidoctorFile.getSectionId(reader.getLineNumber() - 2);
		try {
			String packagePath = getPackagePath(sectionId);
			List<Include> includes = new ArrayList<>();
			for (Language language : Language.values()) {
				Include include = Include.load(document, language, includeAttributes, packagePath, target);
				if (include != null) {
					includes.add(include);
				}
			}
			reader.push_include(getIncludeData(includes), null, null, 0, Collections.emptyMap());
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"Error processing code include " + target + " in " + sectionId + " : " + ex.getMessage(), ex);
		}
	}

	private String getIncludeAttributes(Map<String, Object> attributes) {
		StringBuilder markup = new StringBuilder();
		markup.append("[");
		attributes.forEach((key, value) -> markup.append(key + "=" + value));
		markup.append("]");
		return markup.toString();
	}

	private String getIncludeData(List<Include> includes) {
		if (includes.isEmpty()) {
			throw new IllegalStateException("Unable to find code");
		}
		StringBuilder data = new StringBuilder();
		for (int i = 0; i < includes.size(); i++) {
			String role = (includes.size() == 1) ? null : (i == 0) ? "primary" : "secondary";
			includes.get(i).append(role, data);
		}
		return data.toString();
	}

	private String getPackagePath(String sectionId) {
		return sectionId.replace('.', '/').replace("-", "");
	}

	static class AsciidoctorFile {

		private final List<String> lines;

		AsciidoctorFile(String filename) {
			try {
				this.lines = Files.readAllLines(Paths.get(filename));
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		String getSectionId(int lineNumber) {
			while (lineNumber >= 0) {
				String line = this.lines.get(lineNumber).trim();
				if (line.startsWith("[[") && line.endsWith("]]")) {
					return line.substring(2, line.length() - 2);
				}
				lineNumber--;
			}
			throw new IllegalStateException("Cannot find anchor");

		}

	}

	static class Include {

		private final Language language;

		private final String includeAttributes;

		private Path source;

		Include(Language language, String includeAttributes, Path source) throws IOException {
			this.language = language;
			this.includeAttributes = includeAttributes;
			this.source = source;
		}

		void append(String role, StringBuilder data) {
			data.append("[source," + this.language.getId() + ",indent=0,subs=\"verbatim\"");
			if (role != null) {
				data.append("role=" + role);
			}
			data.append("]\n");
			if (role != null) {
				data.append("." + this.language.getTitle() + "\n");
			}
			data.append("----\n");
			data.append("include::" + this.source.toAbsolutePath().toString() + this.includeAttributes + "\n");
			data.append("----\n");
		}

		static Include load(Document document, Language language, String includeAttributes, String packagePath,
				String target) throws IOException {
			String path = (String) document.getAttribute(language.getPathAttribute());
			if (path != null) {
				Path source = cleanPath(path + "/" + packagePath + "/" + target + "." + language.GetExtension());
				if (Files.isRegularFile(source)) {
					return new Include(language, includeAttributes, source);
				}
			}
			return null;
		}

		private static Path cleanPath(String path) {
			path = path.replace("//", "/");
			List<String> elements = new ArrayList<>();
			for (String element : path.split("\\/")) {
				if ("..".equals(element) && !elements.isEmpty()) {
					elements.remove(elements.size() - 1);
				}
				else {
					elements.add(element);
				}
			}
			String cleanPath = elements.stream().collect(Collectors.joining("/"));
			cleanPath = (path.startsWith("/")) ? "/" + cleanPath : cleanPath;
			return Paths.get(cleanPath);
		}

	}

}
