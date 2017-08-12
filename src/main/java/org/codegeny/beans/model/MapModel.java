package org.codegeny.beans.model;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

public final class MapModel<M, K, V> implements Model<M>, Function<M, Map<? extends K, ? extends V>> {
	
	private final Function<? super M, ? extends Map<? extends K, ? extends V>> extractor;
	private final Model<? super K> keyDelegate;
	private final Model<? super V> valueDelegate;
	
	MapModel(Function<? super M, ? extends Map<? extends K, ? extends V>> extractor, Model<? super K> keyDelegate, Model<? super V> valueDelegate) {
		this.extractor = requireNonNull(extractor);
		this.keyDelegate = requireNonNull(keyDelegate);
		this.valueDelegate = requireNonNull(valueDelegate);
	}

	@Override
	public <R> R accept(ModelVisitor<? extends M, ? extends R> visitor) {
		return requireNonNull(visitor).visitMap(this);
	}
	
	public <R> R acceptKeyDelegate(ModelVisitor<? extends K, ? extends R> visitor) {
		return this.keyDelegate.accept(visitor);
	}
	
	public <R> R acceptValueDelegate(ModelVisitor<? extends V, ? extends R> visitor) {
		return this.valueDelegate.accept(visitor);
	}
	
	@Override
	public Map<? extends K, ? extends V> apply(M values) {
		return values == null ? emptyMap() : this.extractor.apply(values);
	}

	public Model<? super K> getKeyDelegate() {
		return this.keyDelegate;
	}

	public Model<? super V> getValueDelegate() {
		return this.valueDelegate;
	}
}
