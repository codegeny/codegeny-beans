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

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Visitor pattern for <code>{@link Diff}</code>s.
 *
 * @param <T> The type of <code>{@link Diff}&lt;T&gt;</code>.
 * @param <R> The type of the result.
 * @author Xavier DURY
 */
public interface DiffVisitor<T, R> {

    /**
     * Create a visitor which process all type of {@link Diff} with the same function.
     *
     * @param function The function.
     * @param <T>      The type of <code>{@link Diff}&lt;T&gt;</code>.
     * @param <R>      The type of the result.
     * @return A visitor.
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
     * Visitor which returns the {@link Diff} itself.
     *
     * @param <T> The type of <code>{@link Diff}&lt;T&gt;</code>.
     * @return A visitor.
     */
    static <T> DiffVisitor<T, Diff<T>> identity() {
        return adapter(Function.identity());
    }

    /**
     * Visitor which combine the result of the current visitor and the result of the given visitor.
     *
     * @param that     The other visitor.
     * @param combiner The combining function.
     * @param <X>      The other other visitor return type.
     * @param <Y>      The type of the combined values (&lt;R&gt; and &lt;X&gt;).
     * @return A visitor.
     */
    default <X, Y> DiffVisitor<T, Y> andThen(DiffVisitor<T, ? extends X> that, BiFunction<? super R, ? super X, ? extends Y> combiner) {
        return adapter(diff -> combiner.apply(diff.accept(this), diff.accept(that)));
    }

    /**
     * Visit a <code>{@link BeanDiff}&lt;T&gt;</code>
     *
     * @param bean The bean to visit.
     * @return The computed result.
     */
    R visitBean(BeanDiff<T> bean);

    /**
     * Visit a <code>{@link ListDiff}&lt;T, E&gt;</code>
     *
     * @param list The list to visit.
     * @param <E>  The type of elements.
     * @return The computed result.
     */
    <E> R visitList(ListDiff<T, E> list);

    /**
     * Visit a <code>{@link MapDiff}&lt;T, K, V&gt;</code>
     *
     * @param map The map to visit.
     * @param <K> The type of the map key.
     * @param <V> The type of the map value.
     * @return The computed result.
     */
    <K, V> R visitMap(MapDiff<T, K, V> map);

    /**
     * Visit a <code>{@link SimpleDiff}&lt;T&gt;</code>
     *
     * @param simple The simple value to visit.
     * @return The computed result.
     */
    R visitSimple(SimpleDiff<T> simple);
}
