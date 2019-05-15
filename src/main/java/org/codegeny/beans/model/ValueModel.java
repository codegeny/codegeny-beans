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
import static java.util.Comparator.nullsLast;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;

/**
 * An implementation of {@link Model} for a simple/atomic value.
 *
 * @param <V> The value type.
 * @author Xavier DURY
 */
public final class ValueModel<V> implements Model<V> {
	
	private final Comparator<? super V> comparator;
	private final Class<? extends V> type;
	
	ValueModel(Class<? extends V> type, Comparator<? super V> comparator) {
		this.type = requireNonNull(type);
		this.comparator = nullsLast(requireNonNull(comparator));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<V, ? extends R> visitor) {
		return requireNonNull(visitor).visitValue(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(V left, V right) {
		return comparator.compare(left, right);
	}
	
	/**
	 * Return the value class.
	 * 
	 * @return The value class.
	 */
	public Class<? extends V> getType() {
		return type;
	}
}
