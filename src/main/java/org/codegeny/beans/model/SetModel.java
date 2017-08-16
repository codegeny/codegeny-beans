package org.codegeny.beans.model;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Function;

public final class SetModel<S, E> implements Model<S>, Function<S, Set<? extends E>> {

	private final Model<? super E> elementModel;
	private final Function<? super S, ? extends Set<? extends E>> extractor;

	SetModel(Function<? super S, ? extends Set<? extends E>> extractor, Model<? super E> elementModel) {
		this.extractor = requireNonNull(extractor);
		this.elementModel = requireNonNull(elementModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<? extends S, ? extends R> visitor) {
		return requireNonNull(visitor).visitSet(this);
	}

	public <R> R acceptElement(ModelVisitor<? extends E, ? extends R> visitor) {
		return this.elementModel.accept(visitor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<? extends E> apply(S values) {
		return values == null ? emptySet() : this.extractor.apply(values);
	}

	public Model<? super E> getElementModel() {
		return this.elementModel;
	}
}