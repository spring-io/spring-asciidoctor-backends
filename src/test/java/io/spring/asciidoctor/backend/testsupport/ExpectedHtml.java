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

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An expected snippet of HTML.
 *
 * @author Phillip Webb
 * @see AsciidoctorExtension
 */
public class ExpectedHtml implements Consumer<String> {

	private final String expected;

	ExpectedHtml(String expected) {
		this.expected = expected;
	}

	@Override
	public void accept(String actual) {
		System.out.println(actual);
		assertThat(actual).isEqualTo(this.expected);
	}

}
