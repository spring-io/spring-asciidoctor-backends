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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Processor;

/**
 * {@link BlockMacroProcessor} to support {@code include-code} imports.
 *
 * @author Phillip Webb
 */
@Name("include-code")
public class IncludeCodeBlockMacroProcessor extends BlockMacroProcessor {

	@Override
	public Object process(StructuralNode parent, String target, Map<String, Object> attributes) {
		try {
			Document document = parent.getDocument();
			Path root = getDirectory(document).toPath();
			String sectionId = getSectionId(parent);
			String packagePath = getPackagePath(sectionId);
			List<Content> contents = new ArrayList<>();
			for (Language language : Language.values()) {
				Content content = Content.load(document, language, root, packagePath, target);
				if (content != null) {
					contents.add(content);
				}
			}
			return process(parent, contents);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Error processing include-code::" + target + " : " + ex.getMessage(), ex);
		}
	}

	private File getDirectory(Document document) {
		Object directory = document.getOptions().get("base_dir");
		directory = (directory != null) ? directory : document.getOptions().get("docdir");
		directory = (directory != null) ? directory : new File(".");
		return (directory instanceof File) ? (File) directory : new File(directory.toString());
	}

	private Object process(StructuralNode parent, List<Content> contents) {
		if (contents.isEmpty()) {
			throw new IllegalStateException("Unable to find code");
		}
		Block container = createBlock(parent, "open", (String) null);
		if (contents.size() == 1) {
			container.append(contents.get(0).createBlock(this, parent));
			return container;
		}
		boolean primary = true;
		for (Content content : contents) {
			Block block = content.createBlock(this, container);
			block.setAttribute("role", (primary) ? "primary" : "secondary", true);
			container.append(block);
			primary = false;
		}
		return container;
	}

	private String getSectionId(ContentNode node) {
		Section section = getSection(node);
		String sectionId = section.getId();
		return sectionId;
	}

	private Section getSection(ContentNode node) {
		while (node != null) {
			if (node instanceof Section) {
				return (Section) node;
			}
			node = node.getParent();
		}
		throw new IllegalStateException("Unable to find section");
	}

	private String getPackagePath(String sectionId) {
		return sectionId.replace('.', '/').replace("-", "");
	}

	static class Content {

		private final Language language;

		private final String content;

		Content(Language language, Path source) throws IOException {
			this.language = language;
			try (Stream<String> lines = Files.lines(source)) {
				this.content = lines.collect(Collectors.joining("\n"));
			}
		}

		Block createBlock(Processor processor, StructuralNode parent) {
			Block block = processor.createBlock(parent, "listing", this.content);
			block.setStyle("source");
			block.setAttribute("language", this.language.getLanguage(), true);
			block.setAttribute("indent", 0, true);
			block.setAttribute("subs", "verbatim", true);
			block.setTitle(this.language.getTitle());
			return block;
		}

		static Content load(Document document, Language language, Path root, String packagePath, String target)
				throws IOException {
			String path = (String) document.getAttribute(language.getPathAttribute());
			if (path != null) {
				Path source = root.resolve(path).resolve(packagePath).resolve(target + "." + language.GetExtension());
				if (Files.isRegularFile(source)) {
					return new Content(language, source);
				}
			}
			return null;
		}

	}

}
