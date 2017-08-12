package org.codegeny.beans.diff;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Visitor pattern for <code>{@linkplain Diff}</code>s.
 * 
 * @author Xavier DURY
 * @param <T> The type of <code>{@linkplain Diff}&lt;T&gt;</code>.
 * @param <R> The type of the result.
 */
public interface DiffVisitor<T, R> {
	
	/**
	 * Create a visitor which process all type of {@linkplain Diff} with the same function.
	 * 
	 * @param function The function.
	 * @return A visitor.
	 * @param <T> The type of <code>{@linkplain Diff}&lt;T&gt;</code>.
	 * @param <R> The type of the result.
	 */
	static <T, R> DiffVisitor<T, R> adapter(Function<? super Diff<T>, ? extends R> function) {
		return new DiffVisitor<T, R>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public R visitBean(BeanDiff<T> bean) {
				return function.apply(bean);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <E> R visitList(ListDiff<T, E> list) {
				return function.apply(list);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <K, V> R visitMap(MapDiff<T, K, V> map) {
				return function.apply(map);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public R visitSimple(SimpleDiff<T> simple) {
				return function.apply(simple);
			}
		};
	}
	
	/**
	 * Visitor which returns the {@linkplain Diff} itself.
	 * 
	 * @return A visitor.
	 * @param <T> The type of <code>{@linkplain Diff}&lt;T&gt;</code>.
	 */
	static <T> DiffVisitor<T, Diff<T>> identity() {
		return adapter(Function.identity());
	}
	
	/**
	 * Visitor which combine the result of the current visitor and the result of the given visitor.
	 * 
	 * @param that The other visitor.
	 * @param combiner The combining function.
	 * @return A visitor.
	 * @param <X> The other other visitor return type.
	 * @param <Y> The type of the combined values (&lt;R&gt; and &lt;X&gt;).
	 */
	default <X, Y> DiffVisitor<T, Y> andThen(DiffVisitor<T, ? extends X> that, BiFunction<? super R, ? super X, ? extends Y> combiner) {
		return adapter(diff -> combiner.apply(diff.accept(this), diff.accept(that)));
	}
	
	/**
	 * Visit a <code>{@linkplain BeanDiff}&lt;T&gt;</code>
	 * 
	 * @param bean The bean to visit.
	 * @return The computed result.
	 */
	R visitBean(BeanDiff<T> bean);
	
	/**
	 * Visit a <code>{@linkplain ListDiff}&lt;T, E&gt;</code>
	 * 
	 * @param list The list to visit.
	 * @return The computed result.
	 * @param <E> The type of elements.
	 */
	<E> R visitList(ListDiff<T, E> list);
	
	/**
	 * Visit a <code>{@linkplain MapDiff}&lt;T, K, V&gt;</code>
	 * 
	 * @param map The map to visit.
	 * @return The computed result.
	 * @param <K> The type of the map key.
	 * @param <V> The type of the map value.
	 */
	<K, V> R visitMap(MapDiff<T, K, V> map);
	
	/**
	 * Visit a <code>{@linkplain SimpleDiff}&lt;T&gt;</code>
	 * 
	 * @param simple The simple value to visit.
	 * @return The computed result.
	 */
	R visitSimple(SimpleDiff<T> simple);
}