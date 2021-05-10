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

package io.spring.asciidoctor.backend.testsupport;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Represents a converted HTML document.
 *
 * @author Phillip Webb
 * @see AsciidoctorExtension
 */
public class ConvertedPdf {

	private final PDDocument document;

	private final String text;

	ConvertedPdf(Path pdf) throws IOException {
		this.document = PDDocument.load(pdf.toFile());
		this.text = new PDFTextStripper().getText(this.document);
	}

	@Override
	public String toString() {
		return this.text;
	}

}
