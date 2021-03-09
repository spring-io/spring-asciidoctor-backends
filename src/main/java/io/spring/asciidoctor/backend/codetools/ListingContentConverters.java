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
import java.util.Collections;
import java.util.List;

import org.asciidoctor.ast.Block;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.jruby.RubyObject;

/**
 * Utility called by the Ruby code to apply modifications to code listings.
 *
 * @author Phillip Webb
 */
public final class ListingContentConverters {

	private static final List<ListingContentConverter> converters;
	static {
		List<ListingContentConverter> converterInstances = new ArrayList<>();
		converterInstances.add(new ChompListingContentConverter());
		converterInstances.add(new FoldListingContentConverter());
		converters = Collections.unmodifiableList(converterInstances);
	}

	private ListingContentConverters() {
	}

	/**
	 * Convert the content of the given node before it is added to the HTML.
	 * @param node the node to convert
	 * @return the converted content
	 */
	public static RubyObject content(RubyObject node) {
		Block listingBlock = (Block) NodeConverter.createASTNode(node);
		String content = (String) listingBlock.getContent();
		for (ListingContentConverter converter : converters) {
			content = (content != null) ? content : "";
			content = converter.convert(listingBlock, content);
		}
		return node.getRuntime().newString(content != null ? content : "");
	}

}
