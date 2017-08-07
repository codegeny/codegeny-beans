package org.codegeny.beans.model;

import static java.util.Comparator.nullsLast;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;

public final class ValueModel<T> implements Model<T>, Comparator<T> {
	
	private final Comparator<? super T> comparator;
	
	ValueModel(Comparator<? super T> comparator) {
		this.comparator = nullsLast(requireNonNull(comparator));
	}

	@Override
	public <R> R accept(ModelVisitor<? extends T, ? extends R> visitor) {
		return requireNonNull(visitor).visitValue(this);
	}
	
	@Override
	public int compare(T left, T right) {
		return this.comparator.compare(left, right);
	}
}