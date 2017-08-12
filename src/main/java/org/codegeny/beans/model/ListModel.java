package org.codegeny.beans.model;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Function;

public final class ListModel<C, E> implements Model<C>, Function<C, List<? extends E>> {

	private final Model<? super E> delegate;
	private final Function<? super C, ? extends List<? extends E>> extractor;

	ListModel(Function<? super C, ? extends List<? extends E>> extractor, Model<? super E> delegate) {
		this.extractor = requireNonNull(extractor);
		this.delegate = requireNonNull(delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<? extends C, ? extends R> visitor) {
		return requireNonNull(visitor).visitList(this);
	}

	public <R> R acceptDelegate(ModelVisitor<? extends E, ? extends R> visitor) {
		return this.delegate.accept(visitor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<? extends E> apply(C values) {
		return values == null ? emptyList() : this.extractor.apply(values);
	}

	public Model<? super E> getDelegate() {
		return this.delegate;
	}
}