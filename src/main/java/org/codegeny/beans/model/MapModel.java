package org.codegeny.beans.model;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

public final class MapModel<M, K, V> implements Model<M>, Function<M, Map<? extends K, ? extends V>> {
	
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
