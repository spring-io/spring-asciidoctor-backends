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

package io.spring.asciidoctor.backend.testsupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Stream;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit extension for {@link Asciidoctor} tests.
 *
 * @author Phillip Webb
 */
public class AsciidoctorExtension implements ParameterResolver {

	private static final Namespace NAMESPACE = Namespace.create(AsciidoctorExtension.class);

	private static final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Class<?> type = parameterContext.getParameter().getType();
		return ConvertedHtml.class.isAssignableFrom(type) || ConvertedHtmlSupplier.class.isAssignableFrom(type)
				|| ExpectedHtml.class.isAssignableFrom(type) || ConvertedPdf.class.isAssignableFrom(type);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Class<?> type = parameterContext.getParameter().getType();
		if (ConvertedHtmlSupplier.class.isAssignableFrom(type)) {
			return (ConvertedHtmlSupplier) () -> resolveConvertedHtml(parameterContext, extensionContext, true);
		}
		if (ConvertedHtml.class.isAssignableFrom(type)) {
			return resolveConvertedHtml(parameterContext, extensionContext, false);
		}
		if (ExpectedHtml.class.isAssignableFrom(type)) {
			return resolveExpectedHtml(parameterContext, extensionContext);
		}
		if (ConvertedPdf.class.isAssignableFrom(type)) {
			return resolveConvertedPdf(parameterContext, extensionContext);
		}
		return null;
	}

	private ConvertedHtml resolveConvertedHtml(ParameterContext parameterContext, ExtensionContext extensionContext,
			boolean supplier) {
		return resolveConverted(parameterContext, extensionContext, supplier, "spring-html",
				(path, name) -> new ConvertedHtml(path, path.resolve(name + ".html")));
	}

	private ExpectedHtml resolveExpectedHtml(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> testClass = extensionContext.getTestClass().get();
		Method testMethod = extensionContext.getTestMethod().get();
		String expectedHtmlFilename = getFilename(testClass, testMethod,
				"_" + parameterContext.getParameter().getName() + ".html");
		try {
			try (Reader reader = getReader(testClass, expectedHtmlFilename)) {
				return new ExpectedHtml(readFully(reader));
			}
		}
		catch (Exception ex) {
			throw new ParameterResolutionException("Error reading expected HTML file " + expectedHtmlFilename, ex);
		}
	}

	private ConvertedPdf resolveConvertedPdf(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return resolveConverted(parameterContext, extensionContext, false, "spring-pdf",
				(path, name) -> new ConvertedPdf(path.resolve(name + ".pdf")));
	}

	private <R> R resolveConverted(ParameterContext parameterContext, ExtensionContext extensionContext,
			boolean supplier, String backend, ResultFactory<R> resultFactory) {
		Class<?> testClass = extensionContext.getTestClass().get();
		Method testMethod = extensionContext.getTestMethod().get();
		String asciidocFilename = parameterContext.findAnnotation(Source.class).map(Source::value)
				.orElseGet(() -> getFilename(testClass, testMethod, ".adoc"));
		try {
			Temp temp = Temp.get(extensionContext);
			String name = "test";
			Path source = temp.path().resolve(name + ".adoc");
			try (InputStream inputStream = testClass.getResourceAsStream(asciidocFilename)) {
				assertThat(inputStream).as(testClass + " " + asciidocFilename).isNotNull();
				Files.copy(inputStream, source, StandardCopyOption.REPLACE_EXISTING);
			}
			InputStream anchorRewrite = testClass
					.getResourceAsStream(asciidocFilename.replace(".adoc", "-anchor-rewrite.properties"));
			if (anchorRewrite != null) {
				Files.copy(anchorRewrite, temp.path().resolve("anchor-rewrite.properties"),
						StandardCopyOption.REPLACE_EXISTING);
				anchorRewrite.close();
			}
			copyRelatedFolder(testClass, asciidocFilename.replace(".adoc", ""), temp.path());
			OptionsBuilder options = OptionsBuilder.options();
			options.safe(SafeMode.UNSAFE);
			options.baseDir(temp.directory());
			options.backend(backend);
			options.toDir(temp.directory());
			asciidoctor.convertFile(source.toFile(), options);
			return resultFactory.create(temp.path(), name);
		}
		catch (Exception ex) {
			if (supplier) {
				if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				}
				throw new IllegalStateException(ex);
			}
			throw new ParameterResolutionException("Error converting asciidoc " + asciidocFilename, ex);
		}
	}

	private Reader getReader(Class<?> testClass, String expectedHtmlFilename) {
		InputStream inputStream = testClass.getResourceAsStream(expectedHtmlFilename);
		assertThat(inputStream).as(expectedHtmlFilename).isNotNull();
		return new InputStreamReader(inputStream);
	}

	private String readFully(Reader reader) {
		try (Scanner scanner = new Scanner(reader).useDelimiter("\\A")) {
			return (scanner.hasNext()) ? scanner.next() : "";
		}
	}

	private String getFilename(Class<?> testClass, Method testMethod, String suffix) {
		String testClassName = testClass.getName();
		String root = testClassName.substring(testClassName.lastIndexOf('.') + 1);
		return root + "_" + testMethod.getName() + suffix;
	}

	private void copyRelatedFolder(Class<?> testClass, String folder, Path destination) throws Exception {
		URL resource = testClass.getResource(folder);
		Path source = (resource != null) ? Paths.get(resource.toURI()) : null;
		if (source != null && Files.isDirectory(source)) {
			Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Files.createDirectories(destination.resolve(source.relativize(dir)));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path relative = source.relativize(file).getParent()
							.resolve(file.getFileName().toString().replace("._", "."));
					Files.copy(file, destination.resolve(relative));
					return FileVisitResult.CONTINUE;
				}

			});
		}
	}

	private static class Temp implements CloseableResource {

		private final Path directory;

		Temp() throws IOException {
			this.directory = Files.createTempDirectory("junitasciidoc");
		}

		File directory() {
			return this.directory.toFile();
		}

		Path path() {
			return this.directory;
		}

		@Override
		public void close() throws IOException {
			try (Stream<Path> walk = Files.walk(this.directory)) {
				walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		}

		static Temp get(ExtensionContext extensionContext) {
			Store store = extensionContext.getStore(NAMESPACE);
			return store.getOrComputeIfAbsent("output.dir", (key) -> Temp.create(), Temp.class);
		}

		static Temp create() {
			try {
				return new Temp();
			}
			catch (Exception ex) {
				throw new ExtensionConfigurationException("Failed to create output directory", ex);
			}
		}

	}

	interface ResultFactory<R> {

		R create(Path path, String name) throws IOException;

	}

}
