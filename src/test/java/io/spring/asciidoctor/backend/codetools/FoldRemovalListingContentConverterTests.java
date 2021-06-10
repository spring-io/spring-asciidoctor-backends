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

import io.spring.asciidoctor.backend.testsupport.AsciidoctorExtension;
import io.spring.asciidoctor.backend.testsupport.ConvertedPdf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FoldRemovalListingContentConverter}.
 *
 * @author Phillip Webb
 */
@ExtendWith(AsciidoctorExtension.class)
class FoldRemovalListingContentConverterTests {

	@Test
	void convertWhenHasFoldCommentReturnsRemoved(ConvertedPdf pdf) {
		assertThat(pdf.toString()).doesNotContain("@fold");
	}

	@Test
	void convertWhenHasCodeFoldingDisabledAsDocumentAttributeReturnsUnchanged(ConvertedPdf pdf) {
		assertThat(pdf.toString()).contains("@fold");
	}

}
