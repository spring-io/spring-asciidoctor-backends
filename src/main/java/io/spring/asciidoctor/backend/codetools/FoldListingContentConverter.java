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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.asciidoctor.backend.language.Language;
import org.asciidoctor.ast.Block;

/**
 * {@link ListingContentConverter} to add folding markup.
 *
 * @author Phillip Webb
 */
class FoldListingContentConverter implements ListingContentConverter {

	@Override
	public String convert(Block listingBlock, String content) {
		Options<FoldOption> options = Options.get(listingBlock, "fold", FoldOption.class, FoldOption.DEFAULTS);
		Language language = Language.get(listingBlock);
		if (Language.isJavaLike(language)) {
			List<Folder> folders = new ArrayList<>();
			if (options.has(FoldOption.IMPORTS)) {
				folders.add(new ImportsFolder());
			}
			if (options.has(FoldOption.TAGS)) {
				folders.add(new TagFolder("//", "@fold:on", "@fold:off"));
			}
			FoldBlocks foldBlocks = FoldBlocks.get(content, new Folders(folders));
			if (foldBlocks.isFoldable()) {
				return convert(foldBlocks);
			}
		}
		return content;
	}

	private String convert(FoldBlocks foldBlocks) {
		StringBuilder html = new StringBuilder();
		Iterator<FoldBlock> iterator = foldBlocks.iterator();
		while (iterator.hasNext()) {
			FoldBlock foldBlock = iterator.next();
			String whenFoldedContent = foldBlock.getWhenFoldedContent();
			if (whenFoldedContent != null && !whenFoldedContent.isEmpty()) {
				html.append("<span class=\"fold-block hide-when-unfolded\">");
				html.append(indentMatch(foldBlock, whenFoldedContent));
				html.append("\n\n");
				html.append("</span>");
			}
			html.append("<span class=\"fold-block");
			if (foldBlock.hasFolder()) {
				html.append(" hide-when-folded");
			}
			html.append("\">");
			writeLines(html, foldBlock, iterator.hasNext());
			html.append("</span>");
		}
		return html.toString();
	}

	private void writeLines(StringBuilder html, Iterable<String> lines, boolean endWithNewLine) {
		String lastLine = null;
		boolean writtenLine = false;
		for (String line : lines) {
			if (isBlankLine(line) && !writtenLine) {
				continue;
			}
			html.append(line);
			html.append("\n");
			writtenLine = true;
			lastLine = line;
		}
		html.append((!isBlankLine(lastLine) && endWithNewLine) ? "\n" : "");
	}

	private boolean isBlankLine(String line) {
		return line != null && line.trim().isEmpty();
	}

	private String indentMatch(FoldBlock foldBlock, String line) {
		for (String blockLine : foldBlock) {
			if (!isBlankLine(blockLine)) {
				StringBuilder indent = new StringBuilder();
				for (char ch : blockLine.toCharArray()) {
					if (Character.isWhitespace(ch)) {
						indent.append(ch);
					}
					else {
						return indent + line;
					}
				}
			}
		}
		return line;
	}

	/**
	 * A collections of {@link Folder} instances.
	 */
	private static class Folders {

		private final List<Folder> folders;

		Folders(List<Folder> folders) {
			this.folders = Collections.unmodifiableList(folders);
		}

		Folder get(String[] lines, int index) {
			for (Folder folder : this.folders) {
				if (folder.isStart(lines, index)) {
					return folder;
				}
			}
			return null;
		}

	}

	/**
	 * Strategy used to fold content.
	 */
	private interface Folder {

		boolean isStart(String[] lines, int index);

		boolean isEnd(String[] lines, int index);

		default boolean isStartConsumed() {
			return false;
		}

		default boolean isEndConsumed() {
			return false;
		}

		default String getWhenFoldedContent(String line) {
			return null;
		}

	}

	/**
	 * A Folder that looks for tags in code comments that indicate a fold.
	 */
	private static class TagFolder implements Folder {

		private final String prefix;

		private final String on;

		private final String off;

		TagFolder(String prefix, String on, String off) {
			this.prefix = prefix;
			this.on = on;
			this.off = off;
		}

		@Override
		public boolean isStart(String[] lines, int index) {
			return isPrefixed(lines[index]) && lines[index].contains(this.on);
		}

		@Override
		public boolean isEnd(String[] lines, int index) {
			return isPrefixed(lines[index]) && lines[index].contains(this.off);
		}

		private boolean isPrefixed(String line) {
			return line.trim().startsWith(this.prefix);
		}

		@Override
		public String getWhenFoldedContent(String foldStartLine) {
			return foldStartLine.substring(foldStartLine.indexOf(this.on) + this.on.length()).trim();
		}

	}

	private static class ImportsFolder implements Folder {

		@Override
		public boolean isStart(String[] lines, int index) {
			return isImport(lines[index]);
		}

		@Override
		public boolean isEnd(String[] lines, int index) {
			if ((index + 1) < lines.length) {
				String nextLine = lines[index + 1];
				return !(nextLine.trim().isEmpty() || isImport(nextLine));
			}
			return true;
		}

		private boolean isImport(String line) {
			return line.startsWith("import ");
		}

		@Override
		public boolean isStartConsumed() {
			return true;
		}

		@Override
		public boolean isEndConsumed() {
			return true;
		}

	}

	/**
	 * A collection of {@link FoldBlock} instances.
	 */
	private static class FoldBlocks implements Iterable<FoldBlock> {

		private final List<FoldBlock> foldBlocks;

		FoldBlocks(Stream<FoldBlock> areas) {
			this.foldBlocks = Collections.unmodifiableList(areas.collect(Collectors.toList()));
		}

		boolean isFoldable() {
			return this.foldBlocks.stream().anyMatch(FoldBlock::hasFolder);
		}

		@Override
		public Iterator<FoldBlock> iterator() {
			return this.foldBlocks.iterator();
		}

		static FoldBlocks get(String content, Folders folders) {
			return get(content.split("\n\r?"), folders);
		}

		static FoldBlocks get(String[] lines, Folders folders) {
			List<FoldBlock> foldBlocks = new ArrayList<>();
			Folder folder = null;
			int start = 0;
			for (int i = 0; i < lines.length; i++) {
				if (folder != null) {
					if (folder.isEnd(lines, i)) {
						foldBlocks.add(createFoldArea(folder, lines, start, i));
						start = i + 1;
						folder = null;
					}
				}
				else {
					folder = folders.get(lines, i);
					if (folder != null) {
						foldBlocks.add(createFoldArea(null, lines, start, i - 1));
						start = i;
					}
				}
			}
			if (start < lines.length) {
				foldBlocks.add(createFoldArea(folder, lines, start, lines.length - 1));
			}
			return new FoldBlocks(foldBlocks.stream().filter(FoldBlock::hasLines));
		}

		private static FoldBlock createFoldArea(Folder folder, String[] lines, int start, int end) {
			String foldStartLine = lines[start];
			int fromIndex = (folder == null || folder.isStartConsumed()) ? start : start + 1;
			int toIndex = (folder == null || folder.isEndConsumed()) ? end + 1 : end;
			List<String> blockLines = Collections.unmodifiableList(Arrays.asList(lines).subList(fromIndex, toIndex));
			return new FoldBlock(folder, foldStartLine, blockLines);
		}

	}

	/**
	 * An individual fold area.
	 */
	private static class FoldBlock implements Iterable<String> {

		private final Folder folder;

		private final String foldStartLine;

		private List<String> lines;

		FoldBlock(Folder folder, String foldStartLine, List<String> lines) {
			this.folder = folder;
			this.foldStartLine = foldStartLine;
			this.lines = lines;
		}

		@Override
		public Iterator<String> iterator() {
			return this.lines.iterator();
		}

		boolean hasFolder() {
			return this.folder != null;
		}

		boolean hasLines() {
			return !this.lines.isEmpty();
		}

		String getWhenFoldedContent() {
			return (this.folder != null) ? this.folder.getWhenFoldedContent(this.foldStartLine) : null;
		}

		@Override
		public String toString() {
			return this.lines.stream().collect(Collectors.joining("\n"));
		}

	}

}
