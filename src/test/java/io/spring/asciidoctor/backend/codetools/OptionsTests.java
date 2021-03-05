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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Options}.
 *
 * @author Phillip Webb
 */
class OptionsTests {

	@Test
	void parseWhenHasDocumentReturnsExpected() {
		Options<TestOption> options = Options.parse("one two", "", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.ONE, TestOption.TWO);
	}

	@Test
	void parseWhenHasDocumentNoneReturnsEmpty() {
		Options<TestOption> options = Options.parse("none", "", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).isEmpty();
	}

	@Test
	void parseWhenHasDocumentAllReturnsAll() {
		Options<TestOption> options = Options.parse("all", "", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.values());
	}

	@Test
	void parseWhenHasDocumentWithAddsAndRemovesReturnsExpected() {
		Options<TestOption> options = Options.parse("all -three -one", "", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.TWO);
	}

	@Test
	void parseWhenHasDocumentWithBlockReturnsBlockValue() {
		Options<TestOption> options = Options.parse("all", "two", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.TWO);
	}

	@Test
	void parseWhenHasDocumentWithBlockRemovalReturnsMerged() {
		Options<TestOption> options = Options.parse("one two", "-two", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.ONE);
	}

	@Test
	void parseWhenHasDocumentWithBlockAdditionReturnsMerged() {
		Options<TestOption> options = Options.parse("one", "+two", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.ONE, TestOption.TWO);
	}

	@Test
	void parseWhenNoneReturnsDefaults() {
		Options<TestOption> options = Options.parse("", "", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.ONE, TestOption.THREE);
	}

	@Test
	void whenHasDocumentNoneWithBlockAdditionReturnsDefaultsWithAddition() {
		Options<TestOption> options = Options.parse("", "+two", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.ONE, TestOption.TWO, TestOption.THREE);

	}

	@Test
	void whenHasDocumentNoneWithBlockRemovalReturnsDefaultWithRemoval() {
		Options<TestOption> options = Options.parse("", "-three", TestOption.class, TestOption.DEFAULTS);
		assertThat(options).containsExactly(TestOption.ONE);
	}

	enum TestOption {

		ONE, TWO, THREE;

		static TestOption[] DEFAULTS = { ONE, THREE };

	}

}
