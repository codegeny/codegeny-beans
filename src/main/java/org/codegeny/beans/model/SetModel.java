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

import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link Model} for a set.
 *
 * @param <S> The type of the set.
 * @param <E> The type of the element.
 * @author Xavier DURY
 */
public final class SetModel<S, E> implements Model<S> {

    /**
     * The element model.
     */
    private final Model<E> elementModel;

    /**
     * A function to convert the type &gt;L&gt; to Set&gt;E&lt;.
     */
    private final Function<? super S, ? extends Set<E>> converter;

    /**
     * Constructor.
     *
     * @param elementModel The element model.
     * @param converter    A function to convert the type &gt;L&gt; to Set&gt;E&lt;.
     */
    SetModel(Model<E> elementModel, Function<? super S, ? extends Set<E>> converter) {
        this.elementModel = requireNonNull(elementModel);
        this.converter = requireNonNull(converter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(ModelVisitor<S, ? extends R> visitor) {
        return requireNonNull(visitor).visitSet(this);
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
     * Transform a set of type &lt;S&gt; to Set&lt;E&gt;
     *
     * @param values The values.
     * @return A set of elements.
     */
    public Set<E> toSet(S values) {
        return values == null ? emptySet() : converter.apply(values);
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
