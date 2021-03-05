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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.asciidoctor.ast.Block;

/**
 * Utility used to get user defined options.
 *
 * @param <E> the option enum type
 * @author Phillip Webb
 */
final class Options<E extends Enum<E>> implements Iterable<E> {

	private final Set<Option> options;

	private final Set<E> active;

	private Options(Class<E> type, String value, boolean defaultWhenEmpty, E[] defaults) {
		this.options = parse(type, value, defaultWhenEmpty);
		this.active = getActive(type, this.options, defaults);
	}

	private Options(Class<E> type, Set<Option> options, E[] defaults) {
		this.options = options;
		this.active = getActive(type, this.options, defaults);
	}

	private Set<Option> parse(Class<E> type, String value, boolean defaultWhenEmpty) {
		if (value == null || value.isEmpty()) {
			return (!defaultWhenEmpty) ? Collections.emptySet() : Collections.singleton(new Option(OptionType.DEFAULT));
		}
		Set<Option> options = new LinkedHashSet<>();
		for (String optionValue : value.split("\\s")) {
			options.add(new Option(type, optionValue));
		}
		return Collections.unmodifiableSet(options);
	}

	private Set<E> getActive(Class<E> type, Set<Option> options, E[] defaults) {
		Set<E> active = EnumSet.noneOf(type);
		for (Option option : options) {
			if (option.getType() == OptionType.DEFAULT) {
				active.addAll(Arrays.asList(defaults));
			}
			if (option.getType() == OptionType.ALL) {
				active.addAll(EnumSet.allOf(type));
			}
			else if (option.getType() == OptionType.NONE) {
				active.clear();
			}
			else if (option.getType() == OptionType.ADD || option.getType() == OptionType.VALUE) {
				active.add(option.getValue());
			}
			else if (option.getType() == OptionType.REMOVE) {
				active.remove(option.getValue());
			}
		}
		return Collections.unmodifiableSet(active);
	}

	Options<E> merge(Class<E> type, E[] defaults, Options<E> other) {
		if (other.hasAnyOptionOfType(OptionType.ALL, OptionType.NONE, OptionType.VALUE, OptionType.DEFAULT)) {
			return other;
		}
		Set<Option> options = new LinkedHashSet<>(this.options);
		options.addAll(other.options);
		return new Options<>(type, options, defaults);
	}

	private boolean hasAnyOptionOfType(OptionType... optionTypes) {
		Predicate<OptionType> predicate = Arrays.asList(optionTypes)::contains;
		return this.options.stream().map(Option::getType).anyMatch(predicate);
	}

	boolean has(E option) {
		return this.active.contains(option);
	}

	@Override
	public Iterator<E> iterator() {
		return this.active.iterator();
	}

	static <E extends Enum<E>> Options<E> get(Block block, String name, Class<E> type, E[] defaults) {
		String documentAttribute = (String) block.getDocument().getAttribute(name);
		String blockAttribute = (String) block.getAttribute(name);
		return parse(documentAttribute, blockAttribute, type, defaults);
	}

	static <E extends Enum<E>> Options<E> parse(String documentAttribute, String blockAttribute, Class<E> type,
			E[] defaults) {
		Options<E> documentOptions = new Options<>(type, documentAttribute, true, defaults);
		Options<E> blockOptions = new Options<>(type, blockAttribute, false, defaults);
		return documentOptions.merge(type, defaults, blockOptions);
	}

	private class Option {

		private final OptionType type;

		private final E value;

		Option(Class<E> type, String value) {
			if (value.equalsIgnoreCase("all")) {
				this.type = OptionType.ALL;
				this.value = null;
			}
			else if (value.equalsIgnoreCase("none")) {
				this.type = OptionType.NONE;
				this.value = null;
			}
			else if (value.equalsIgnoreCase("default")) {
				this.type = OptionType.DEFAULT;
				this.value = null;
			}
			else if (value.startsWith("+")) {
				this.type = OptionType.ADD;
				this.value = parseValue(type, value.substring(1));
			}
			else if (value.startsWith("-")) {
				this.type = OptionType.REMOVE;
				this.value = parseValue(type, value.substring(1));
			}
			else {
				this.type = OptionType.VALUE;
				this.value = parseValue(type, value);
			}
		}

		Option(OptionType type) {
			this.type = type;
			this.value = null;
		}

		private E parseValue(Class<E> type, String value) {
			return Enum.valueOf(type, value.toUpperCase());
		}

		OptionType getType() {
			return this.type;
		}

		E getValue() {
			return this.value;
		}

	}

	private enum OptionType {

		VALUE, ADD, REMOVE, ALL, NONE, DEFAULT;

	}

}
