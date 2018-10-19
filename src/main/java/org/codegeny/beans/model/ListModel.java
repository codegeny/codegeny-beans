package org.codegeny.beans.model;

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