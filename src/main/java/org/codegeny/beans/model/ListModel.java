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
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Function;

public final class ListModel<L, E> implements Model<L>, Function<L, List<E>> {

	private final Model<E> elementModel;
	private final Function<? super L, ? extends List<E>> extractor;

	ListModel(Function<? super L, ? extends List<E>> extractor, Model<E> elementModel) {
		this.extractor = requireNonNull(extractor);
		this.elementModel = requireNonNull(elementModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<L, ? extends R> visitor) {
		return requireNonNull(visitor).visitList(this);
	}

	public <R> R acceptElement(ModelVisitor<E, ? extends R> visitor) {
		return this.elementModel.accept(visitor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<E> apply(L values) {
		return values == null ? emptyList() : this.extractor.apply(values);
	}

	public Model<E> getElementModel() {
		return this.elementModel;
	}
}
