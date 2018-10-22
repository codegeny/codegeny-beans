package org.codegeny.beans.model;

/*-
 * #%L
 * codegeny-beans
 * %%
 * Copyright (C) 2016 - 2018 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
