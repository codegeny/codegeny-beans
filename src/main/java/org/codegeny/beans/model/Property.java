package org.codegeny.beans.model;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

public final class Property<B, P> implements Function<B, P> {
	
	private final Model<? super P> delegate;
	private final Function<? super B, ? extends P> extractor;
	private final String name;
	
	Property(String name, Function<? super B, ? extends P> extractor, Model<? super P> delegate) {
		this.name = requireNonNull(name);
		this.extractor = requireNonNull(extractor);
		this.delegate = requireNonNull(delegate);
	}
	
	public <R> R acceptDelegate(ModelVisitor<? extends P, ? extends R> visitor) {
		return this.delegate.accept(visitor);
	}
	
	@Override
	public P apply(B bean) {
		return bean == null ? null : this.extractor.apply(bean);
	}
	
	public Model<? super P> getDelegate() {
		return this.delegate;
	}
	
	public String getName() {
		return this.name;
	}
}