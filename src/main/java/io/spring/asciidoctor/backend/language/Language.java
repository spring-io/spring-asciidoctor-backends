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

package io.spring.asciidoctor.backend.language;

import org.asciidoctor.ast.ContentNode;

/**
 * Languages supported by the backend.
 *
 * @author Phillip Webb
 */
public enum Language {

	/**
	 * Java.
	 */
	JAVA("Java", "docs-java", "java", true, ";"),

	/**
	 * Kotlin.
	 */
	KOTLIN("Kotlin", "docs-kotlin", "kt", true, ""),

	/**
	 * Groovy.
	 */
	GROOVY("Groovy", "docs-groovy", "groovy", true, "");

	private final String title;

	private final String pathAttribute;

	private final String extension;

	private final boolean javaLike;

	private final String statementTerminator;

	Language(String title, String pathAttribute, String extension, boolean javaLike, String statementTerminator) {
		this.title = title;
		this.pathAttribute = pathAttribute;
		this.extension = extension;
		this.javaLike = javaLike;
		this.statementTerminator = statementTerminator;
	}

	public String getTitle() {
		return this.title;
	}

	public String getPathAttribute() {
		return this.pathAttribute;
	}

	public String GetExtension() {
		return this.extension;
	}

	public String getId() {
		return this.name().toLowerCase();
	}

	public boolean isJavaLike() {
		return this.javaLike;
	}

	public String getStatementTerminator() {
		return this.statementTerminator;
	}

	public static Language get(ContentNode node) {
		String language = (String) node.getAttribute("language");
		for (Language candidate : values()) {
			if (candidate.getId().equals(language)) {
				return candidate;
			}
		}
		return null;

	}

	public static boolean isJavaLike(Language language) {
		return language != null && language.isJavaLike();
	}

}
