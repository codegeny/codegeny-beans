package org.codegeny.beans.diff;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.Map;

/**
 * Implementation of <code>{@link Diff}</code> for beans.
 *
 * @author Xavier DURY
 * @param <B> The type of bean.
 */
public final class BeanDiff<B> extends AbstractDiff<B> {
	
	private static final long serialVersionUID = 1L;

	private final Map<String, Diff<?>> properties;

	BeanDiff(Status status, B left, B right, Map<String, ? extends Diff<?>> properties) {
		super(requireNonNull(properties, "Properties cannot be null").values(), status, left, right);
		this.properties = unmodifiableMap(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(DiffVisitor<B, R> visitor) {
		return visitor.visitBean(this);
	}

	/**
	 * Get the map of diff'ed properties.
	 * 
	 * @return The properties.
	 */
	public Map<String, Diff<?>> getProperties() {
		return properties;
	}
	
	/**
	 * Get the diff'ed for the given property name.
	 * 
	 * @param name The property name.
	 * @return That property's diff.
	 */
	public Diff<?> getProperty(String name) {
		return properties.get(name);
	}
}