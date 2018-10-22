package org.codegeny.beans.model;

/*-
 * #%L
 * codegeny-beans
 * %%
 * Copyright (C) 2016 - 2018 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class Property<B, P> {
	
	/**
	 * Construct a property to be used for beans.
	 * 
	 * @param name The property name.
	 * @param extractor The property extractor/getter.
	 * @param mutator The property mutator/Setter.
	 * @param model The property model.
	 * @return A property.
	 * @param <B> The bean type.
	 * @param <P> The property type.
	 */
	public static <B, P> Property<B, P> mutable(String name, Function<? super B, P> extractor, BiConsumer<? super B, P> mutator, Model<P> model) {
		return new Property<>(name, extractor, mutator, model);
	}
	
	/**
	 * Construct a read-only property to be used for beans.
	 * 
	 * @param name The property name.
	 * @param extractor The property extractor/getter.
	 * @param model The property model.
	 * @return A property.
	 * @param <B> The bean type.
	 * @param <P> The property type.
	 */
	public static <B, P> Property<B, P> immutable(String name, Function<? super B, P> extractor,  Model<P> model) {
		return new Property<>(name, extractor, (b, p) -> {
			throw new UnsupportedOperationException(String.format("Property '%s' is immutable", name));
		} , model);
	}
	
	private final Model<P> model;
	private final Function<? super B, ? extends P> extractor;
	private final BiConsumer<? super B, ? super P> mutator;
	private final String name;
	
	private Property(String name, Function<? super B, ? extends P> extractor, BiConsumer<? super B, P> mutator, Model<P> model) {
		this.name = requireNonNull(name);
		this.extractor = requireNonNull(extractor);
		this.mutator = requireNonNull(mutator);
		this.model = requireNonNull(model);
	}
	
	public <R> R accept(ModelVisitor<P, ? extends R> visitor) {
		return this.model.accept(visitor);
	}
	
	public P get(B bean) {
		return bean == null ? null : this.extractor.apply(bean);
	}
	
	public void set(B bean, P value) {
		if (bean != null) {
			this.mutator.accept(bean, value);
		}
	}
	
	public Model<P> getModel() {
		return this.model;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public boolean equals(Object that) {
		return this == that || that instanceof Property<?, ?> && Objects.equals(this.name, ((Property<?, ?>) that).name);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
