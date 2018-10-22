package org.codegeny.beans.diff;

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
