package org.codegeny.beans.model;

import static java.util.Comparator.nullsLast;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;

public final class ValueModel<V> implements Model<V>, Comparator<V> {
	
	private final Comparator<? super V> comparator;
	
	ValueModel(Comparator<? super V> comparator) {
		this.comparator = nullsLast(requireNonNull(comparator));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<? extends V, ? extends R> visitor) {
		return requireNonNull(visitor).visitValue(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(V left, V right) {
		return this.comparator.compare(left, right);
	}
}