package org.codegeny.beans.model;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * An implementation of {@link Model} for a bean.
 *  
 * @author Xavier DURY
 * @param <B> The type of the bean.
 */
public final class BeanModel<B> implements Model<B>, Iterable<Property<B, ?>> {
	
	private final Class<B> type;
	private final Map<String, Property<B, ?>> properties;
	
	BeanModel(Class<B> type, Map<String, Property<B, ?>> properties) {
		this.type = requireNonNull(type);
		this.properties = requireNonNull(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<B, ? extends R> visitor) {
		return requireNonNull(visitor).visitBean(this);
	}

	/**
	 * Get the properties which are registered for this <code>BeanModel</code>.
	 * 
	 * @return The properties.
	 */
	public Collection<Property<B, ?>> getProperties() {
		return unmodifiableCollection(this.properties.values());
	}

	/**
	 * Get a property by its name.
	 * 
	 * @param name The name of the property.
	 * @return The corresponding property or null.
	 */
	public Property<B, ?> getProperty(String name) {
		return properties.get(requireNonNull(name));
	}
	
	/**
	 * Return the bean class.
	 * 
	 * @return The bean class.
	 */
	public Class<B> getType() {
		return type;
	}
	
	/**
	 * Iterate over all properties.
	 * 
	 * @return An iterator of all properties.
	 */
	@Override
	public Iterator<Property<B, ?>> iterator() {
		return getProperties().iterator();
	}
}
