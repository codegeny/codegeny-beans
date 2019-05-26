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
package org.codegeny.beans.model;

/**
 * Visitor for a {@link Model} which can return a result of type &gt;R&lt;
 *
 * @param <T> The type of the model.
 * @param <R> The result type.
 * @author Xavier DURY
 */
public interface ModelVisitor<T, R> {

    /**
     * Visit a {@link BeanModel}.
     *
     * @param bean The bean model.
     * @return The result.
     */
    R visitBean(BeanModel<T> bean);

    /**
     * Visit a {@link ListModel}.
     *
     * @param list The list model.
     * @param <E>  The element type of the list model.
     * @return The result.
     */
    <E> R visitList(ListModel<T, E> list);

    /**
     * Visit a {@link MapModel}.
     *
     * @param map the map model.
     * @param <K> The key type of the map model.
     * @param <V> The value type of the map model.
     * @return the result.
     */
    <K, V> R visitMap(MapModel<T, K, V> map);

    /**
     * Visit a {@link SetModel}.
     *
     * @param set The set model.
     * @param <E> The element type of the set model.
     * @return The result.
     */
    <E> R visitSet(SetModel<T, E> set);

    /**
     * Visit a {@link ValueModel}.
     *
     * @param value The value model.
     * @return The result.
     */
    R visitValue(ValueModel<T> value);
}
