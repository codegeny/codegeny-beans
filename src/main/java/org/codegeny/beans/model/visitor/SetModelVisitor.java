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
import org.codegeny.beans.model.Typer;
import org.codegeny.beans.model.ValueModel;
import org.codegeny.beans.path.Path;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.function.Predicate.isEqual;

/**
 * Visitor which sets a value inside an object structure based on a given path.
 *
 * @param <S> The path-element and value bottom type.
 * @param <T> The object structure type.
 * @author Xavier DURY
 */
public final class SetModelVisitor<S, T> implements ModelVisitor<T, Void> {

    /**
     * The current node in the object structure.
     */
    private final T current;

    /**
     * The value to set inside the object structure.
     */
    private final S valueToSet;

    /**
     * An iterator of path elements to get to the place in the object structure where the given value must be set.
     */
    private final Iterator<? extends S> path;

    /**
     * The setter.
     */
    private final Consumer<? super T> setter;

    /**
     * The typer to convert path elements and value to set to the correct type.
     */
    private final Typer<S> typer;

    /**
     * Constructor.
     *
     * @param current    The current node in the object structure.
     * @param valueToSet The value to set inside the object structure.
     * @param path       The path to get to the place in the object structure where the given value must be set.
     * @param typer      The typer to convert path elements and value to set to the correct type.
     */
    public SetModelVisitor(T current, S valueToSet, Path<S> path, Typer<S> typer) {
        this(current, valueToSet, path.iterator(), typer, a -> {
            throw new UnsupportedOperationException("Cannot set root object");
        });
    }

    /**
     * Constructor.
     *
     * @param current    The current node in the object structure.
     * @param valueToSet The value to set inside the object structure.
     * @param path       The path elements iterator to get to the place in the object structure where the given value must be set.
     * @param typer      The typer to convert path elements and value to set to the correct type.
     * @param setter     The setter.
     */
    private SetModelVisitor(T current, S valueToSet, Iterator<? extends S> path, Typer<S> typer, Consumer<? super T> setter) {
        this.current = current;
        this.valueToSet = valueToSet;
        this.path = path;
        this.typer = typer;
        this.setter = setter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitBean(BeanModel<T> beanModel) {
        return followNestedPathOrSetValue(k -> visitProperty(beanModel.getProperty(typer.retype(Model.STRING, k))), setter, beanModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Void visitMap(MapModel<T, K, V> mapModel) {
        Map<K, V> map = mapModel.toMap(current);
        return setNested(mapModel.getValueModel(), mapModel.getKeyModel(), map::get, map::put, mapModel, newMap -> {
            map.clear();
            map.putAll(mapModel.toMap(newMap));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Void visitSet(SetModel<T, E> setModel) {
        Set<E> set = setModel.toSet(current);
        return setNested(setModel.getElementModel(), setModel.getElementModel(), value -> set.stream().filter(isEqual(value)).findAny().orElse(null), (a, b) -> set.add(a), setModel, newSet -> {
            set.clear();
            set.addAll(setModel.toSet(newSet));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Void visitList(ListModel<T, E> listModel) {
        List<E> list = listModel.toList(current);
        return setNested(listModel.getElementModel(), Model.INTEGER, list::get, list::set, listModel, newList -> {
            list.clear();
            list.addAll(listModel.toList(newList));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitValue(ValueModel<T> valueModel) {
        return followNestedPathOrSetValue(pathElement -> {
            throw new UnsupportedOperationException("Value object must be terminal");
        }, setter, valueModel);
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     */
    private <P> void visitProperty(Property<? super T, P> property) {
        property.accept(newVisitor(property.get(current), value -> property.set(current, value)));
    }

    /**
     * Either go deeper down the path if there are any path elements left or set the value.
     *
     * @param pathElementCallback A callback to apply on the next path element to set the value.
     * @return Nothing.
     */
    private Void followNestedPathOrSetValue(Consumer<? super S> pathElementCallback, Consumer<? super T> setter, Model<? extends T> valueModel) {
        if (path.hasNext()) {
            pathElementCallback.accept(path.next());
        } else {
            setter.accept(typer.retype(valueModel, valueToSet));
        }
        return null;
    }

    /**
     * Follow a path by converting the next path element to a key (property name, index, map key...) then sets the value.
     *
     * @param nestedModel  The element model.
     * @param keyModel     The model of the key.
     * @param nestedGetter A function which takes a key of type K and returns an element of type E.
     * @param nestedSetter A consumer which takes a key of type K and an element of type E and sets it on the current node.
     * @param model        The current model.
     * @param setter       The current setter.
     * @param <K>          The key type.
     * @param <E>          The nested type.
     * @return Nothing.
     */
    private <K, E> Void setNested(Model<E> nestedModel, Model<? extends K> keyModel, Function<? super K, ? extends E> nestedGetter, BiConsumer<? super K, ? super E> nestedSetter, Model<? extends T> model, Consumer<? super T> setter) {
        return followNestedPathOrSetValue(pathElement -> {
            K key = typer.retype(keyModel, pathElement);
            nestedModel.accept(newVisitor(nestedGetter.apply(key), value -> nestedSetter.accept(key, value)));
        }, setter, model);
    }

    /**
     * Create a new {@link SetModelVisitor} for the given value.
     *
     * @param current The new current.
     * @param setter  The setter.
     * @param <Z>     The type of the new current value.
     * @return A visitor.
     */
    private <Z> SetModelVisitor<S, Z> newVisitor(Z current, Consumer<? super Z> setter) {
        return new SetModelVisitor<>(current, valueToSet, path, typer, setter);
    }
}
