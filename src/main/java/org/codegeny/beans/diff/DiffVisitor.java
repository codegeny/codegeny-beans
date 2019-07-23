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

/**
 * Visitor pattern for <code>{@link Diff}</code>s.
 *
 * @param <T> The type of <code>{@link Diff}&lt;T&gt;</code>.
 * @param <R> The type of the result.
 * @author Xavier DURY
 */
public interface DiffVisitor<T, R> {

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
