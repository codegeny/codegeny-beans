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

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

public final class MapModel<M, K, V> implements Model<M>, Function<M, Map<K, V>> {
	
	private final Function<? super M, ? extends Map<K, V>> extractor;
	private final Model<K> keyModel;
	private final Model<V> valueModel;
	
	MapModel(Function<? super M, ? extends Map<K, V>> extractor, Model<K> keyModel, Model<V> valueModel) {
		this.extractor = requireNonNull(extractor);
		this.keyModel = requireNonNull(keyModel);
		this.valueModel = requireNonNull(valueModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<M, ? extends R> visitor) {
		return requireNonNull(visitor).visitMap(this);
	}
	
	public <R> R acceptKey(ModelVisitor<K, ? extends R> visitor) {
		return this.keyModel.accept(visitor);
	}
	
	public <R> R acceptValue(ModelVisitor<V, ? extends R> visitor) {
		return this.valueModel.accept(visitor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<K, V> apply(M values) {
		return values == null ? emptyMap() : this.extractor.apply(values);
	}

	public Model<K> getKeyModel() {
		return this.keyModel;
	}

	public Model<V> getValueModel() {
		return this.valueModel;
	}
}
