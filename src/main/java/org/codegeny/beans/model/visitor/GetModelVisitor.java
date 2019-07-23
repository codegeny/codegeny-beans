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
import org.codegeny.beans.path.Converter;
import org.codegeny.beans.path.Path;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementation of {@link ModelVisitor} which extracts a value from a {@link Model} based on a {@link Path}.
 *
 * @param <S> The path element type.
 * @param <T> The target.
 * @author Xavier DURY
 */
public final class GetModelVisitor<S, T> implements ModelVisitor<T, Object> {

    /**
     * The current object node.
     */
    private final T current;

    /**
     * The path elements.
     */
    private final Iterator<? extends S> path;

    /**
     * The converter.
     */
    private final Converter<? super S> converter;

    /**
     * Constructor.
     *
     * @param current   The current object node.
     * @param path      The path.
     * @param converter The converter.
     */
    public GetModelVisitor(T current, Path<? extends S> path, Converter<? super S> converter) {
        this(current, path.iterator(), converter);
    }

    /**
     * Constructor.
     *
     * @param current   The current object node.
     * @param path      The path elements iterator.
     * @param converter The converter.
     */
    private GetModelVisitor(T current, Iterator<? extends S> path, Converter<? super S> converter) {
        this.current = current;
        this.path = path;
        this.converter = converter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visitBean(BeanModel<T> beanModel) {
        return followNestedPathOrGetValue(propertyPath -> visitProperty(beanModel.getProperty(converter.convert(String.class, propertyPath))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Object visitMap(MapModel<T, K, V> mapModel) {
        Map<K, V> map = mapModel.toMap(current);
        return getNested(mapModel.getValueModel(), mapModel.getKeyModel(), map::get);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Object visitSet(SetModel<T, E> setModel) {
        Set<E> set = setModel.toSet(current);
        return getNested(setModel.getElementModel(), Model.INTEGER, index -> set.stream().skip(index).findAny().orElse(null));
//		return get(set.getElementModel(), set.getElementModel(), v -> s.stream().filter(isEqual(v)).findAny().orElse(null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Object visitList(ListModel<T, E> listModel) {
        List<E> list = listModel.toList(current);
        return getNested(listModel.getElementModel(), Model.INTEGER, list::get);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visitValue(ValueModel<T> valueModel) {
        return followNestedPathOrGetValue(pathElement -> {
            throw new UnsupportedOperationException("Value object must be terminal");
        });
    }

    /**
     * Extract from a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     * @return The extracted value.
     */
    private <P> Object visitProperty(Property<? super T, P> property) {
        return property.accept(newVisitor(property.get(current)));
    }

    /**
     * Either go deeper down the path if there are any path elements left or return the current value.
     *
     * @param pathElementCallback A callback to apply on the next path element to return the result (extracted value).
     * @return The extracted value.
     */
    private Object followNestedPathOrGetValue(Function<? super S, ?> pathElementCallback) {
        return path.hasNext() ? pathElementCallback.apply(path.next()) : current;
    }

    /**
     * Follow a path by converting the next path element to a key (property name, index, map key...) and extracting the
     * corresponding value from the current object.
     *
     * @param nestedModel  The nested model.
     * @param keyModel     The model of the key.
     * @param nestedGetter A function which takes a key of type K and returns an element of type E.
     * @param <K>          The key type.
     * @param <E>          The nested type.
     * @return The extracted value.
     */
    private <K, E> Object getNested(Model<E> nestedModel, Model<? super K> keyModel, Function<? super K, ? extends E> nestedGetter) {
        return followNestedPathOrGetValue(pathElement -> nestedModel.accept(newVisitor(nestedGetter.apply(converter.convert(keyModel.accept(new TypeModelVisitor<>()), pathElement)))));
    }

    /**
     * Create a new {@link GetModelVisitor} for the given value.
     *
     * @param current The new current.
     * @param <Z>     The type of the new current value.
     * @return A visitor.
     */
    private <Z> GetModelVisitor<S, Z> newVisitor(Z current) {
        return new GetModelVisitor<>(current, path, converter);
    }
}
