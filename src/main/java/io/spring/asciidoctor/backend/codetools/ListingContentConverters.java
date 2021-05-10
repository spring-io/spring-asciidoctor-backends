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

import java.util.Collections;
import java.util.List;

import org.asciidoctor.ast.Block;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.jruby.RubyObject;

/**
 * A collection of {@link ListingContentConverter} instances.
 *
 * @author Phillip Webb
 */
public class ListingContentConverters {

	private final List<ListingContentConverter> converters;

	ListingContentConverters(List<ListingContentConverter> converters) {
		this.converters = Collections.unmodifiableList(converters);
	}

	RubyObject content(RubyObject node) {
		Block listingBlock = (Block) NodeConverter.createASTNode(node);
		String content = (String) listingBlock.getContent();
		for (ListingContentConverter converter : this.converters) {
			content = (content != null) ? content : "";
			content = converter.convert(listingBlock, content);
		}
		return node.getRuntime().newString((content != null) ? content : "");
	}

}
