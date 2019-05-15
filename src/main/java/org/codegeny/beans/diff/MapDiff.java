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
package org.codegeny.beans.diff;

import static java.util.Collections.unmodifiableMap;

import java.util.Map;

/**
 * Implementation of <code>{@link Diff}</code> for maps.
 *
 * @author Xavier DURY
 * @param <M> The type of map.
 * @param <K> The type of map key.
 * @param <V> The type of map value.
 */
public final class MapDiff<M, K, V> extends AbstractDiff<M> {
	
	private static final long serialVersionUID = 1L;

	private final Map<K, Diff<V>> map;

	MapDiff(Status status, M left, M right, Map<K, ? extends Diff<V>> map) {
		super(map.values(), status, left, right);
		this.map = unmodifiableMap(map);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(DiffVisitor<M, R> visitor) {
		return visitor.visitMap(this);
	}

	/**
	 * Get the map of diff'ed values.
	 * 
	 * @return The map.
	 */
	public Map<K, Diff<V>> getMap() {
		return map;
	}
}
