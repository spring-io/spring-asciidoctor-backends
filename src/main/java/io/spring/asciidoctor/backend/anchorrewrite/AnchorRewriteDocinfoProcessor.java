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
import java.io.IOException;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.DocinfoProcessor;

/**
 * {@link DocinfoProcessor} to add an anchor-rewrite script.
 *
 * @author Phillip Webb
 */
public class AnchorRewriteDocinfoProcessor extends DocinfoProcessor {

	@Override
	public String process(Document document) {
		try {
			File directory = getDirectory(document);
			File propertiesFile = new File(directory, "anchor-rewrite.properties");
			return (propertiesFile.exists()) ? AnchorRewriteScriptTag.fromPropertiesFile(propertiesFile).getHtml() : "";
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private File getDirectory(Document document) {
		Object directory = document.getOptions().get("base_dir");
		directory = (directory != null) ? directory : document.getOptions().get("docdir");
		directory = (directory != null) ? directory : new File(".");
		return (directory instanceof File) ? (File) directory : new File(directory.toString());
	}

}
