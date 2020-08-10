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

import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link Model} for a list.
 *
 * @param <L> The type of the list.
 * @param <E> The type of the element.
 * @author Xavier DURY
 */
public final class ListModel<L, E> implements Model<L> {

    /**
     * The element model.
     */
    private final Model<E> elementModel;

    /**
     * A function to convert the type &gt;L&gt; to List&gt;E&lt;.
     */
    private final Function<? super L, ? extends List<E>> converter;

    /**
     * Constructor.
     *
     * @param elementModel The element model.
     * @param converter    A function to convert the type &gt;L&gt; to List&gt;E&lt;.
     */
    ListModel(Model<E> elementModel, Function<? super L, ? extends List<E>> converter) {
        this.elementModel = requireNonNull(elementModel);
        this.converter = requireNonNull(converter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(ModelVisitor<L, ? extends R> visitor) {
        return requireNonNull(visitor).visitList(this);
    }

    /**
     * Visit the element model.
     *
     * @param visitor The visitor.
     * @param <R>     The result type.
     * @return A result of type &lt;R&gt;.
     */
    public <R> R acceptElement(ModelVisitor<E, ? extends R> visitor) {
        return elementModel.accept(visitor);
    }

    /**
     * Transform a list of type &lt;L&gt; to List&lt;E&gt;
     *
     * @param values The values.
     * @return A list of elements.
     */
    public List<E> toList(L values) {
        return values == null ? emptyList() : converter.apply(values);
    }

    /**
     * Get the element model.
     *
     * @return The element model.
     */
    public Model<E> getElementModel() {
        return elementModel;
    }
}
