package org.codegeny.beans.model;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

public final class Property<B, P> implements Function<B, P> {
	
	private final Model<? super P> model;
	private final Function<? super B, ? extends P> extractor;
	private final String name;
	
	Property(String name, Function<? super B, ? extends P> extractor, Model<? super P> model) {
		this.name = requireNonNull(name);
		this.extractor = requireNonNull(extractor);
		this.model = requireNonNull(model);
	}
	
	public <R> R accept(ModelVisitor<? extends P, ? extends R> visitor) {
		return this.model.accept(visitor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public P apply(B bean) {
		return bean == null ? null : this.extractor.apply(bean);
	}
	
	public Model<? super P> getModel() {
		return this.model;
	}
	
	public String getName() {
		return this.name;
	}
}