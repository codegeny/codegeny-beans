package org.codegeny.beans.model;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Function;

public final class SetModel<C, E> implements Model<C>, Function<C, Set<? extends E>> {

	private final Model<? super E> delegate;
	private final Function<? super C, ? extends Set<? extends E>> extractor;

	SetModel(Function<? super C, ? extends Set<? extends E>> extractor, Model<? super E> delegate) {
		this.extractor = requireNonNull(extractor);
		this.delegate = requireNonNull(delegate);
	}

	@Override
	public <R> R accept(ModelVisitor<? extends C, ? extends R> visitor) {
		return requireNonNull(visitor).visitSet(this);
	}

	public <R> R acceptDelegate(ModelVisitor<? extends E, ? extends R> visitor) {
		return this.delegate.accept(visitor);
	}

	@Override
	public Set<? extends E> apply(C values) {
		return values == null ? emptySet() : this.extractor.apply(values);
	}

	public Model<? super E> getDelegate() {
		return this.delegate;
	}
}