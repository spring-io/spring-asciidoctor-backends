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

package io.spring.asciidoctor.backend;

import io.spring.asciidoctor.backend.anchorrewrite.AnchorRewriteDocinfoProcessor;
import io.spring.asciidoctor.backend.includecode.IncludeCodeIncludeProcessor;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

/**
 * {@link ExtensionRegistry} used to automatically register the Spring HTML Backend.
 *
 * @author Phillip Webb
 */
public class SpringBackendsExtensionRegistry implements ExtensionRegistry {

	@Override
	public void register(Asciidoctor asciidoctor) {
		asciidoctor.requireLibrary("spring-asciidoctor-backends");
		asciidoctor.javaExtensionRegistry().docinfoProcessor(new AnchorRewriteDocinfoProcessor());
		// asciidoctor.javaExtensionRegistry().blockMacro(new
		// IncludeCodeBlockMacroProcessor());
		asciidoctor.javaExtensionRegistry().includeProcessor(new IncludeCodeIncludeProcessor());
	}

}
