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
package org.codegeny.beans.model.visitor;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.Model;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.codegeny.beans.util.Utils.forEachIndexed;

/**
 * Generic <code>toString()</code> visitor.
 *
 * @param <T> The element type.
 * @author Xavier DURY
 */
public final class ToStringModelVisitor<T> implements ModelVisitor<T, StringBuilder> {

    /**
     * The string builder.
     */
    private final StringBuilder builder;

    /**
     * The string indentation.
     */
    private final String indent;

    /**
     * The object to be transformed to string.
     */
    private final T target;

    /**
     * Constructor.
     *
     * @param target The object to be transformed to string.
     */
    public ToStringModelVisitor(T target) {
        this(target, new StringBuilder(), "");
    }

    /**
     * Constructor.
     *
     * @param target  The object to be transformed to string.
     * @param builder The string builder.
     * @param indent  The string indentation.
     */
    private ToStringModelVisitor(T target, StringBuilder builder, String indent) {
        this.target = target;
        this.builder = builder;
        this.indent = indent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder visitBean(BeanModel<T> bean) {
        this.builder.append("{");
        int count = forEachIndexed(bean.getProperties(), (property, index) -> {
            this.builder.append(index > 0 ? "," : "").append("\n").append(indent).append("  ").append(property.getName()).append(": ");
            visitProperty(property);
        });
        return this.builder.append(count > 0 ? "\n" : "").append(indent).append("}");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder visitValue(ValueModel<T> value) {
        return builder.append(target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> StringBuilder visitMap(MapModel<T, K, V> map) {
        builder.append("[");
        Comparator<K> comparator = map.getKeyModel();
        Map<K, V> entries = map.toMap(target);
        Collection<K> sorted = entries.keySet().stream().sorted(comparator).collect(toList());
        int count = forEachIndexed(sorted, (value, index) -> map.acceptValue(new ToStringModelVisitor<>(entries.get(value), builder.append(index > 0 ? "," : "").append("\n").append(indent).append("  ").append(value).append(": "), indent.concat("  "))));
        return builder.append(count > 0 ? "\n".concat(indent) : "").append("]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> StringBuilder visitSet(SetModel<T, E> values) {
        return visitCollection(values.getElementModel(), values.toSet(target).stream().sorted(values.getElementModel()).collect(toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> StringBuilder visitList(ListModel<T, E> values) {
        return visitCollection(values.getElementModel(), values.toList(target));
    }

    /**
     * Visit a collection.
     *
     * @param elementModel The collection element model.
     * @param collection   The collection.
     * @param <E>          The collection element type.
     * @return The string builder.
     */
    private <E> StringBuilder visitCollection(Model<E> elementModel, Collection<? extends E> collection) {
        builder.append("[");
        int count = forEachIndexed(collection, (value, index) -> elementModel.accept(new ToStringModelVisitor<>(value, builder.append(index > 0 ? "," : "").append("\n").append(indent).append("  "), indent.concat("  "))));
        return builder.append(count > 0 ? "\n".concat(indent) : "").append("]");
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     */
    private <P> void visitProperty(Property<? super T, P> property) {
        property.accept(new ToStringModelVisitor<>(property.get(target), builder, indent.concat("  ")));
    }
}
