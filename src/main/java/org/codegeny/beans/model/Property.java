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
	
	private final String name;
	private final Function<? super B, ? extends P> getter;
	private final BiConsumer<? super B, ? super P> setter;
	private final Model<P> model;
	
	Property(String name, Function<? super B, ? extends P> getter, BiConsumer<? super B, ? super P> setter, Model<P> model) {
		this.name = requireNonNull(name);
		this.getter = requireNonNull(getter);
		this.setter = requireNonNull(setter);
		this.model = requireNonNull(model);
	}
	
	public <R> R accept(ModelVisitor<P, ? extends R> visitor) {
		return model.accept(visitor);
	}
	
	public P get(B bean) {
		return bean == null ? null : getter.apply(bean);
	}
	
	public void set(B bean, P value) {
		if (bean != null) {
			setter.accept(bean, value);
		}
	}
	
	public Model<P> getModel() {
		return model;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object that) {
		return this == that || that instanceof Property<?, ?> && Objects.equals(name, ((Property<?, ?>) that).name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
