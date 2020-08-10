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

import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link Model} for a map.
 *
 * @param <M> The type of the map.
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Xavier DURY
 */
public final class MapModel<M, K, V> implements Model<M> {

    /**
     * The key model.
     */
    private final Model<K> keyModel;

    /**
     * The value model.
     */
    private final Model<V> valueModel;

    /**
     * A function to convert the type &gt;M&gt; to Map&gt;K, V&lt;.
     */
    private final Function<? super M, ? extends Map<K, V>> converter;

    /**
     * Constructor.
     *
     * @param keyModel    The key model.
     * @param valueModel  The value model.
     * @param converter   A function to convert the type &gt;M&gt; to Map&gt;K, V&lt;.
     */
    MapModel(Model<K> keyModel, Model<V> valueModel, Function<? super M, ? extends Map<K, V>> converter) {
        this.converter = requireNonNull(converter);
        this.keyModel = requireNonNull(keyModel);
        this.valueModel = requireNonNull(valueModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(ModelVisitor<M, ? extends R> visitor) {
        return requireNonNull(visitor).visitMap(this);
    }

    /**
     * Visit the key model.
     *
     * @param visitor The visitor.
     * @param <R>     The result type.
     * @return A result of type &lt;R&gt;.
     */
    public <R> R acceptKey(ModelVisitor<K, ? extends R> visitor) {
        return keyModel.accept(visitor);
    }

    /**
     * Visit the value model.
     *
     * @param visitor The visitor.
     * @param <R>     The result type.
     * @return A result of type &lt;R&gt;.
     */
    public <R> R acceptValue(ModelVisitor<V, ? extends R> visitor) {
        return valueModel.accept(visitor);
    }

    /**
     * Transform a map of type &lt;M&gt; to Map&lt;K, V&gt;
     *
     * @param values The values.
     * @return A map of (key, value).
     */
    public Map<K, V> toMap(M values) {
        return values == null ? emptyMap() : converter.apply(values);
    }

    /**
     * Get the key model.
     *
     * @return The key model.
     */
    public Model<K> getKeyModel() {
        return keyModel;
    }

    /**
     * Get the value model.
     *
     * @return The value model.
     */
    public Model<V> getValueModel() {
        return valueModel;
    }
}
