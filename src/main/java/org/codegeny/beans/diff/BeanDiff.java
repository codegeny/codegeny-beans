package org.codegeny.beans.diff;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.Map;

/**
 * Implementation of {@link Diff} for beans.
 *
 * @author Xavier DURY
 * @param <B> The type of bean.
 */
public final class BeanDiff<B> extends AbstractDiff<B> {
	
	private static final long serialVersionUID = 1L;

	private final Map<String, ? extends Diff<?>> properties;

	BeanDiff(Status status, B left, B right, Map<String, ? extends Diff<?>> properties) {
		super(requireNonNull(properties, "Properties cannot be null").values(), status, left, right);
		this.properties = unmodifiableMap(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(DiffVisitor<B, R> visitor) {
		return visitor.visitBeanDiff(this);
	}

	/**
	 * Get the map of diffed properties.
	 * 
	 * @return The properties.
	 */
	public Map<String, ? extends Diff<?>> getProperties() {
		return properties;
	}
	
	/**
	 * Get the diffed for the given property name.
	 * 
	 * @param name The property name.
	 * @return That property's diff.
	 * @param <P> The property type.
	 */
	public <P> Diff<P> getProperty(String name) {
		@SuppressWarnings("unchecked")
		Diff<P> property = (Diff<P>) properties.get(name);
		return property;
	}
}