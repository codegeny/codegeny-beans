package org.codegeny.beans.model;

import static java.util.Comparator.nullsLast;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;

public final class ValueModel<V> implements Model<V>, Comparator<V> {
	
	private final Comparator<? super V> comparator;
	private final Class<? extends V> type;
	
	ValueModel(Class<? extends V> type, Comparator<? super V> comparator) {
		this.type = requireNonNull(type);
		this.comparator = nullsLast(requireNonNull(comparator));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<V, ? extends R> visitor) {
		return requireNonNull(visitor).visitValue(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(V left, V right) {
		return this.comparator.compare(left, right);
	}
	
	public Class<? extends V> getType() {
		return type;
	}
}