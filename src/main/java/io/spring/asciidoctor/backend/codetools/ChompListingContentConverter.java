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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.asciidoctor.backend.language.Language;
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
		Language language = Language.get(listingBlock);
		if (Language.isJavaLike(language)) {
			List<Chomper> chompers = new ArrayList<>();
			if (options.has(ChompOption.HEADERS)) {
				chompers.add(new HeaderChomper(language));
			}
			if (options.has(ChompOption.PACKAGES)) {
				chompers.add(new PackageChomper(language, packageReplacement));
			}
			if (options.has(ChompOption.TAGS)) {
				chompers.add(new TagChomper());
			}
			if (options.has(ChompOption.FORMATTERS)) {
				chompers.add(new FormatterChomper());
			}
			if (options.has(ChompOption.SUPPRESSWARNINGS)) {
				chompers.add(new SuppressWarningsChomper());
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

		private final Pattern pattern;

		HeaderChomper(Language language) {
			this.pattern = Pattern.compile("^.*?(package\\ [\\w\\.]+" + language.getStatementTerminator() + ")",
					Pattern.DOTALL);
		}

		@Override
		public String chomp(String content) {
			Matcher matcher = this.pattern.matcher(content);
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

		private static Pattern pattern;

		private final String replacement;

		PackageChomper(Language language, String replacement) {
			pattern = Pattern.compile("package\\ [\\w\\.]+" + language.getStatementTerminator() + "\\s*",
					Pattern.DOTALL);
			this.replacement = (replacement != null && !replacement.isEmpty())
					? "package " + replacement + language.getStatementTerminator() + "\n\n" : "";
		}

		@Override
		public String chomp(String content) {
			return pattern.matcher(content).replaceFirst(this.replacement);
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

	private static class SuppressWarningsChomper implements Chomper {

		private static final List<Pattern> PATTERNS;
		static {
			List<Pattern> patterns = new ArrayList<>();
			patterns.add(Pattern.compile("@SuppressWarnings\\(.*\\)"));
			patterns.add(Pattern.compile("@Suppress\\(.*\\)"));
			PATTERNS = Collections.unmodifiableList(patterns);
		}

		@Override
		public String chomp(String content) {
			StringBuilder result = new StringBuilder(content.length());
			String[] lines = content.split("\n");
			for (String line : lines) {
				String strippedline = strip(line);
				if (strippedline.equals(line) || !strippedline.trim().isEmpty()) {
					result.append(strippedline);
					result.append("\n");
				}
			}
			return result.toString();
		}

		private String strip(String line) {
			for (Pattern pattern : PATTERNS) {
				line = pattern.matcher(line).replaceAll("");
			}
			return line;
		}

	}

}
