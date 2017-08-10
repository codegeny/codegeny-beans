package org.codegeny.beans.diff;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.codegeny.beans.diff.visitor.TraversingDiffVisitor;
import org.codegeny.beans.path.Path;
import org.codegeny.beans.path.PathVisitor;

/**
 * A diff represent a comparison between 2 objects (left and right). A diff provides a matching score (between 0 and 1 inclusive) and a {@link Status}.
 * All implementations must be immutable, thread-safe and {@link Serializable} (as long as &lt;T&gt; type is also Serializable).
 * 
 * @author Xavier DURY
 * @param <T> The type of the 2 compared objects.
 */
public interface Diff<T> extends Serializable {
	
	/**
	 * The status of the {@link Diff}. Can be
	 * <ul>
	 * <li>ADDED (left value does not exist while right does)</li>
	 * <li>REMOVED (right value does not exist while left does)</li>
	 * <li>MODIFIED (both values exist but are different)</li>
	 * <li>UNCHANGED (both values exist and are the same or both values do not exist)</li>
	 * </ul>
	 */
	enum Status {
		
		ADDED, MODIFIED, REMOVED, UNCHANGED;
		
		/**
		 * Combine 2 statuses given the following rules:
		 * <ol>
		 * <li>2 identical statuses must give the same status</li>
		 * <li>MODIFIED + any status must give MODIFIED</li>
		 * </ol> 
		 * @param that The other status.
		 * @return The combined status.
		 */
		public Status combine(Status that) {
			return equals(requireNonNull(that, "Status cannot be null")) ? this : MODIFIED;
		}
		
		/**
		 * Is this status representing any change?
		 * 
		 * @return True only if this status is UNCHANGED.
		 */
		public boolean isChanged() {
			return !equals(UNCHANGED);
		}
	}
	
	/**
	 * Static method factory for {@linkplain BeanDiff}.
	 * 
	 * @param status The status.
	 * @param left The left bean.
	 * @param right The right bean.
	 * @param properties The diffed properties as a map.
	 * @return A {@linkplain BeanDiff}.
	 * @param <B> The type of the bean.
	 */
	static <B> BeanDiff<B> bean(Status status, B left, B right, Map<String, ? extends Diff<?>> properties) {
		return new BeanDiff<>(status, left, right, properties);
	}
	
	/**
	 * Static method factory for {@linkplain ListDiff}.
	 * 
	 * @param status The status.
	 * @param left The left list.
	 * @param right The right list.
	 * @param list The diffed elements as a list.
	 * @return A {@linkplain ListDiff}
	 * @param <C> The type of list.
	 * @param <E> The type of the list elements.
	 */
	static <C, E> ListDiff<C, E> list(Status status, C left, C right, List<? extends Diff<E>> list) {
		return new ListDiff<>(status, left, right, list);
	}
	
	/**
	 * Static method factory for {@linkplain MapDiff}.
	 * 
	 * @param status The status.
	 * @param left The left map.
	 * @param right The right map.
	 * @param map The diffed values as a map.
	 * @return A {@linkplain MapDiff}.
	 * @param <M> The type of the map.
	 * @param <K> The type of the map keys.
	 * @param <V> The type of the map values.
	 */
	static <M, K, V> MapDiff<M, K, V> map(Status status, M left, M right, Map<K, ? extends Diff<V>> map) {
		return new MapDiff<>(status, left, right, map);
	}
	
	/**
	 * Static method factory for {@linkplain SimpleDiff}.
	 * 
	 * @param score The score.
	 * @param status The status.
	 * @param left The left value.
	 * @param right Rhe right value.
	 * @return A {@linkplain SimpleDiff}.
	 * @param <T> The type of the value.
	 */
	static <T> SimpleDiff<T> simple(double score, Status status, T left, T right) {
		return new SimpleDiff<>(score, status, left, right);
	}
	
	/**
	 * Static method factory for {@linkplain SimpleDiff}.
	 * 
	 * @param status The status.
	 * @param left The left value.
	 * @param right The right value.
	 * @return A {@linkplain SimpleDiff}.
	 * @param <T> The type of the value.
	 */
	static <T> SimpleDiff<T> simple(Status status, T left, T right) {
		return new SimpleDiff<>(status, left, right);
	}
	
	/**
	 * Accept a visitor.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return The result.
	 * @param <R> The visitor result type.
	 */
	<R> R accept(DiffVisitor<T, R> visitor);

	/**
	 * Get the left value for this diff.
	 * 
	 * @return The left value.
	 */
	T getLeft();

	/**
	 * Get the right value for this diff.
	 * 
	 * @return The right value.
	 */

	T getRight();

	/**
	 * The score which has a range of [0; 1].
	 * <ul>
	 * <li>Each terminal value has a score of either 0 (not matched) or 1
	 * (matched).</li>
	 * <li>Each bean has an averaged score of all its properties scores.</li>
	 * <li>Each collection has an averaged score of its elements scores.</li>
	 * <li>Each map has an averaged score of its values scores.</li>
	 * </ul>
	 * 
	 * @return The normalized score ranging from 0 to 1.
	 */
	double getScore();

	/**
	 * Get the type for this diff which can be either ADDED, REMOVED, MODIFIED or
	 * UNCHANGED.
	 * 
	 * @return The type.
	 */
	Status getStatus();

	/**
	 * Transform this diff to a map [path &rarr; @{linkplain Diff}].
	 * 
	 * @param root The name of the root element.
	 * @return A map.
	 */
	default Map<String, Diff<?>> toMap(String root) {
		Map<String, Diff<?>> map = new LinkedHashMap<>();
		traverse((p, d) -> map.put(p.toString(root), d));
		return map;
	}

	/**
	 * Transform this diff to a string.
	 * 
	 * @param root The name of the root element.
	 * @return A string.
	 */
	default String toString(String root) {
		StringBuilder builder = new StringBuilder();
		traverse((p, d) -> builder.append(p.toString(root)).append(" = ").append(d));
		return builder.toString();
	}
	
	/**
	 * Extract the given path from the current diff.
	 * 
	 * @param path The path.
	 * @return The resulting diff.
	 */
	default Diff<?> extract(Path path) {
		return path.accept(this, new PathVisitor<Diff<?>>() {
			
			@Override
			public Diff<?> visitIndex(Diff<?> parent, int index) {
				return ((ListDiff<?, ?>) parent).getList().get(index);
			}

			@Override
			public Diff<?> visitKey(Diff<?> parent, Object key) {
				return ((MapDiff<?, ?, ?>) parent).getMap().get(key);
			}

			@Override
			public Diff<?> visitProperty(Diff<?> parent, String property) {
				return ((BeanDiff<?>) parent).getProperties().get(property);
			}
		});
	}
	
	/**
	 * Traverse the diff tree.
	 * 
	 * @param consumer The consumer.
	 */
	default void traverse(BiConsumer<? super Path, ? super Diff<?>> consumer) {
		accept(new TraversingDiffVisitor<>(consumer));	
	}
}