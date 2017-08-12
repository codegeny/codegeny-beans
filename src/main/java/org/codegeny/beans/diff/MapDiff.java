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

	private final Map<K, ? extends Diff<V>> map;

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
	public Map<K, ? extends Diff<V>> getMap() {
		return map;
	}
}