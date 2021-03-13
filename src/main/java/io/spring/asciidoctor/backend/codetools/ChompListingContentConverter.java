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

package io.spring.asciidoctor.backend.codetools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Block;

/**
 * {@link ListingContentConverter} to chomp content.
 *
 * @author Phillip Webb
 */
public class ChompListingContentConverter implements ListingContentConverter {

	private static final String PACKAGE_REPLACEMENT_ATTR = "chomp_package_replacement";

	@Override
	public String convert(Block listingBlock, String content) {
		Options<ChompOption> options = Options.get(listingBlock, "chomp", ChompOption.class, ChompOption.DEFAULTS);
		String packageReplacement = (String) listingBlock.getAttribute(PACKAGE_REPLACEMENT_ATTR,
				listingBlock.getDocument().getAttribute(PACKAGE_REPLACEMENT_ATTR));
		String lang = (String) listingBlock.getAttribute("language");
		if ("java".equals(lang)) {
			List<Chomper> chompers = new ArrayList<>();
			if (options.has(ChompOption.HEADERS)) {
				chompers.add(new HeaderChomper());
			}
			if (options.has(ChompOption.PACKAGES)) {
				chompers.add(new PackageChomper(packageReplacement));
			}
			if (options.has(ChompOption.TAGS)) {
				chompers.add(new TagChomper());
			}
			if (options.has(ChompOption.FORMATTERS)) {
				chompers.add(new FormatterChomper());
			}
			return chomp(content, chompers);
		}
		return content;
	}

	private String chomp(String content, List<Chomper> chompers) {
		for (Chomper chomper : chompers) {
			content = chomper.chomp(content);
		}
		return content;
	}

	/**
	 * Strategy used to chomp content.
	 */
	private interface Chomper {

		String chomp(String content);

	}

	/**
	 * {@link Chomper} to strip off headers.
	 */
	private static class HeaderChomper implements Chomper {

		private static final Pattern PATTERN = Pattern.compile("^.*?(package\\ [\\w\\.]+;)", Pattern.DOTALL);

		@Override
		public String chomp(String content) {
			Matcher matcher = PATTERN.matcher(content);
			if (matcher.find()) {
				StringBuffer updated = new StringBuffer();
				matcher.appendReplacement(updated, matcher.group(1));
				matcher.appendTail(updated);
				return updated.toString();
			}
			return content;
		}

	}

	/**
	 * {@link Chomper} to remove package declarations.
	 */
	private static class PackageChomper implements Chomper {

		private static final Pattern PATTERN = Pattern.compile("package\\ [\\w\\.]+;\\s*", Pattern.DOTALL);

		private final String replacement;

		PackageChomper(String replacement) {
			this.replacement = (replacement != null && !replacement.isEmpty()) ? "package " + replacement + ";\n\n"
					: "";
		}

		@Override
		public String chomp(String content) {
			return PATTERN.matcher(content).replaceFirst(this.replacement);
		}

	}

	/**
	 * {@link Chomper} to remove content based on comment tags.
	 */
	private static class TagChomper implements Chomper {

		private static final Pattern SHORT_PATTERN = Pattern.compile("\\/\\*\\*\\/.*$", Pattern.MULTILINE);

		private static final Pattern LINE_PATTERN = Pattern.compile("\\/\\*\\s*@chomp:line(.*)\\*\\/.*$",
				Pattern.MULTILINE);

		private static final Pattern SINGLE_LINE_PATTERN = Pattern.compile("\\/\\/\\s*@chomp:line(.*)$",
				Pattern.MULTILINE);

		private static final Pattern FILE_PATTERN = Pattern.compile("(\\/\\/|\\/\\*)\\s*@chomp:file");

		@Override
		public String chomp(String content) {
			content = chompShortPatterns(content);
			content = chompLinePatterns(LINE_PATTERN, content);
			content = chompLinePatterns(SINGLE_LINE_PATTERN, content);
			content = chompFilePatterns(content);
			return content;
		}

		private String chompShortPatterns(String content) {
			return SHORT_PATTERN.matcher(content).replaceAll("...");
		}

		private String chompLinePatterns(Pattern pattern, String content) {
			StringBuffer buffer = new StringBuffer();
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				matcher.appendReplacement(buffer, matcher.group(1).trim());
			}
			matcher.appendTail(buffer);
			return buffer.toString();
		}

		private String chompFilePatterns(String content) {
			Matcher matcher = FILE_PATTERN.matcher(content);
			while (matcher.find()) {
				StringBuffer buffer = new StringBuffer();
				matcher.appendReplacement(buffer, "");
				return buffer.toString();
			}
			return content;
		}

	}

	private static class FormatterChomper implements Chomper {

		private static final Pattern PATTERN = Pattern.compile("^[\\ \\t]*\\/\\/\\s*@formatter:(on|off).*$\n?",
				Pattern.MULTILINE);

		@Override
		public String chomp(String content) {
			return PATTERN.matcher(content).replaceAll("");
		}

	}

}
