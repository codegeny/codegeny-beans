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
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Function;

public final class SetModel<S, E> implements Model<S> {

	private final Model<E> elementModel;
	private final Function<? super S, ? extends Set<E>> extractor;

	SetModel(Function<? super S, ? extends Set<E>> extractor, Model<E> elementModel) {
		this.extractor = requireNonNull(extractor);
		this.elementModel = requireNonNull(elementModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<S, ? extends R> visitor) {
		return requireNonNull(visitor).visitSet(this);
	}

	public <R> R acceptElement(ModelVisitor<E, ? extends R> visitor) {
		return elementModel.accept(visitor);
	}

	public Set<E> toSet(S values) {
		return values == null ? emptySet() : extractor.apply(values);
	}

	public Model<E> getElementModel() {
		return elementModel;
	}
}
