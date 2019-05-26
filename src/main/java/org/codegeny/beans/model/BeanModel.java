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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

/**
 * An implementation of {@link Model} for a bean.
 *
 * @param <B> The type of the bean.
 * @author Xavier DURY
 */
public final class BeanModel<B> implements Model<B> {

    private final Class<? extends B> type;
    private final Map<String, Property<? super B, ?>> properties;

    @SafeVarargs
    BeanModel(Class<? extends B> type, Property<? super B, ?>... properties) {
        this.type = requireNonNull(type);
        this.properties = Stream.of(requireNonNull(properties)).collect(Collectors.toMap(Property::getName, identity()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(ModelVisitor<B, ? extends R> visitor) {
        return requireNonNull(visitor).visitBean(this);
    }

    /**
     * Get the properties which are registered for this <code>BeanModel</code>.
     *
     * @return The properties.
     */
    public Collection<Property<? super B, ?>> getProperties() {
        return unmodifiableCollection(this.properties.values());
    }

    /**
     * Get a property by its name.
     *
     * @param name The name of the property.
     * @return The corresponding property or null.
     */
    public Property<? super B, ?> getProperty(String name) {
        return properties.get(requireNonNull(name));
    }

    /**
     * Return the bean class.
     *
     * @return The bean class.
     */
    public Class<? extends B> getType() {
        return type;
    }
}
