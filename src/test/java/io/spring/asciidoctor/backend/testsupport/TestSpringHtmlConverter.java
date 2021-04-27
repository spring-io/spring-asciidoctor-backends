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

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

/**
 * Utility class used to convert Asciidoctor files for testing.
 *
 * @author Phillip Webb
 */
public class TestSpringHtmlConverter {

	private static final String DEFAULT_DESTINATION = "build/converted-asciidoc";

	private static final String[] DEFAULT_SOURCES = { "src/test/asciidoc", "guides" };

	private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

	void convert(String... args) {
		String destination = (args.length != 0) ? args[0] : DEFAULT_DESTINATION;
		String[] sources = new String[Math.max(args.length - 1, 0)];
		if (sources.length > 0) {
			System.arraycopy(args, 1, sources, 0, sources.length);
		}
		convert(destination, (sources.length != 0) ? sources : DEFAULT_SOURCES);
	}

	private void convert(String destination, String[] sources) {
		File destinationFile = new File(destination);
		File[] sourceFiles = new File[sources.length];
		for (int i = 0; i < sources.length; i++) {
			sourceFiles[i] = new File(sources[i]);
		}
		convert(destinationFile, sourceFiles);
	}

	private void convert(File destination, File[] sources) {
		if (!destination.exists()) {
			destination.mkdirs();
		}
		assertIsDirectory(destination);
		OptionsBuilder optionsBuilder = OptionsBuilder.options().backend("spring-html").toDir(destination)
				.safe(SafeMode.UNSAFE);
		for (File source : sources) {
			Options options = optionsBuilder
					.baseDir(source.isDirectory() ? source.getAbsoluteFile() : source.getAbsoluteFile().getParentFile())
					.get();
			convert(destination, source, options);
		}
	}

	private void convert(File destination, File source, Options options) {
		if (source.isDirectory()) {
			for (File file : source.listFiles(this::isAsciidocFile)) {
				convert(destination, file, options);
			}
		}
		else {
			System.out.println("Converting " + source);
			this.asciidoctor.convertFile(source, options);
		}
	}

	private boolean isAsciidocFile(File file) {
		return file.getName().endsWith(".adoc");
	}

	private void assertIsDirectory(File source) {
		if (!source.exists() || !source.isDirectory()) {
			throw new IllegalArgumentException(source + " is not a directory");
		}
	}

	public static void main(String[] args) {
		new TestSpringHtmlConverter().convert(args);
	}

}
