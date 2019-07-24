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
import org.codegeny.beans.path.Path;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Extract a {@link Model} from another {@link Model} by a given path.
 *
 * @param <T> The model type.
 * @author Xavier DURY
 */
public final class PathToModelVisitor<T> implements ModelVisitor<T, Model<?>> {

    /**
     * The path elements iterator.
     */
    private final Iterator<?> path;

    /**
     * Constructor.
     *
     * @param path The path.
     */
    public PathToModelVisitor(Path<?> path) {
        this(path.iterator());
    }

    /**
     * Constructor.
     *
     * @param path The path elements iterator.
     */
    private PathToModelVisitor(Iterator<?> path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model<?> visitBean(BeanModel<T> bean) {
        return followNestedPathOrGetModel(bean, pathElement -> visitProperty(bean.getProperty((String) pathElement)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Model<?> visitMap(MapModel<T, K, V> map) {
        return getNested(map, map.getValueModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Model<?> visitSet(SetModel<T, E> set) {
        return getNested(set, set.getElementModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Model<?> visitList(ListModel<T, E> list) {
        return getNested(list, list.getElementModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model<?> visitValue(ValueModel<T> value) {
        return followNestedPathOrGetModel(value, pathElement -> {
            throw new UnsupportedOperationException("Value object must be terminal");
        });
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     * @return A model.
     */
    private <P> Model<?> visitProperty(Property<? super T, P> property) {
        return property.accept(newVisitor());
    }

    /**
     * Either go deeper down the path if there are any path elements left or return the current model.
     *
     * @param pathElementCallback A callback to apply on the next path element to return the result (extracted model).
     * @return The extracted model.
     */
    private Model<?> followNestedPathOrGetModel(Model<?> model, Function<Object, Model<?>> pathElementCallback) {
        return path.hasNext() ? pathElementCallback.apply(path.next()) : model;
    }

    /**
     * Follow a path by converting the next path element to a key (property name, index, map key...) and extracting the
     * corresponding model from the current model.
     *
     * @param currentModel The current model.
     * @return The extracted model.
     */
    private <E> Model<?> getNested(Model<T> currentModel, Model<? super E> nestedModel) {
        return followNestedPathOrGetModel(currentModel, pathElement -> nestedModel.accept(newVisitor()));
    }

    /**
     * Create a new visitor.
     *
     * @param <Z> The model type for the new visitor.
     * @return A new visitor.
     */
    private <Z> PathToModelVisitor<Z> newVisitor() {
        return new PathToModelVisitor<>(path);
    }
}
