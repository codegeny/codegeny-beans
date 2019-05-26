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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

/**
 * Property class to be used with {@link BeanModel}.
 *
 * @param <B> The bean type.
 * @param <P> The property type.
 * @author Xavier DURY
 */
public final class Property<B, P> {

    /**
     * {@link Set} wrapper which will delegate {@link Set#add(Object)} to the given adder method.
     *
     * @param <E> The element type.
     */
    private static class WrappedSet<E> extends AbstractSet<E> {

        private final Set<E> set;
        private final Consumer<E> adder;

        WrappedSet(Set<E> set, Consumer<E> adder) {
            this.set = set;
            this.adder = adder;
        }

        @Override
        public Iterator<E> iterator() {
            return set.iterator();
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean add(E element) {
            adder.accept(element);
            return true;
        }
    }

    /**
     * {@link List} wrapper which will delegate {@link List#set(int, Object)} to the given adder method.
     *
     * @param <E> The element type.
     */
    private static class WrappedList<E> extends AbstractList<E> {

        private final List<E> list;
        private final IntFunction<Consumer<E>> adder;

        WrappedList(List<E> list, IntFunction<Consumer<E>> adder) {
            this.list = list;
            this.adder = adder;
        }

        @Override
        public E get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public E set(int index, E element) { // or add ?
            adder.apply(index).accept(element);
            return element;
        }
    }

    private static class WrappedMap<K, V> extends AbstractMap<K, V> {

        private final Map<K, V> map;
        private final Function<K, Consumer<V>> putter;

        WrappedMap(Map<K, V> map, Function<K, Consumer<V>> putter) {
            this.map = map;
            this.putter = putter;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return map.entrySet();
        }

        @Override
        public V put(K key, V value) {
            putter.apply(key).accept(value);
            return value;
        }
    }

    /**
     * If your bean returns a read-only {@link Set} but provides a separate method to add elements to the set, use this method to
     * create a virtual set which will use the adder method when {@link Set#add(Object)} is called.
     *
     * @param getter The set getter.
     * @param adder  The bean adder method.
     * @param <B>    The bean type.
     * @param <E>    The element type.
     * @return A getter function which returns a virtual set.
     * TODO handle removal.
     */
    public static <B, E> Function<B, Set<E>> set(Function<B, Set<E>> getter, BiConsumer<B, E> adder) {
        return bean -> new WrappedSet<>(getter.apply(bean), element -> adder.accept(bean, element));
    }

    /**
     * If your bean returns a read-only {@link List} but provides a separate method to add elements to the list, use this method to
     * create a virtual list which will use the adder method when {@link List#set(int, Object)} is called.
     *
     * @param getter The list getter.
     * @param adder  The bean adder method.
     * @param <B>    The bean type.
     * @param <E>    The element type.
     * @return A getter function which returns a virtual list.
     * TODO handle removal.
     */
    public static <B, E> Function<B, List<E>> list(Function<B, List<E>> getter, Function<B, IntFunction<Consumer<E>>> adder) {
        return bean -> new WrappedList<>(getter.apply(bean), index -> element -> adder.apply(bean).apply(index).accept(element));
    }

    /**
     * If your bean returns a read-only {@link Map} but provides a separate method to put entries to the map, use this method to
     * create a virtual map which will use the put method when {@link Map#put(Object, Object)} is called.
     *
     * @param getter The map getter.
     * @param putter The bean putter method.
     * @param <B>    The bean type.
     * @param <K>    The key type.
     * @param <V>    The value type.
     * @return A getter function which returns a virtual map.
     * TODO handle removal.
     */
    public static <B, K, V> Function<B, Map<K, V>> map(Function<B, Map<K, V>> getter, Function<B, Function<K, Consumer<V>>> putter) {
        return bean -> new WrappedMap<>(getter.apply(bean), key -> value -> putter.apply(bean).apply(key).accept(value));
    }

    /**
     * The property name.
     */
    private final String name;

    /**
     * The property getter method.
     */
    private final Function<? super B, ? extends P> getter;

    /**
     * The property setter method.
     */
    private final BiConsumer<? super B, ? super P> setter;

    /**
     * The property {@link Model}.
     */
    private final Model<P> model;

    Property(String name, Function<? super B, ? extends P> getter, BiConsumer<? super B, ? super P> setter, Model<P> model) {
        this.name = requireNonNull(name);
        this.getter = requireNonNull(getter);
        this.setter = requireNonNull(setter);
        this.model = requireNonNull(model);
    }

    /**
     * Accept a {@link ModelVisitor} for the property.
     *
     * @param visitor A visitor
     * @param <R>     The result type.
     * @return The result.
     */
    public <R> R accept(ModelVisitor<P, ? extends R> visitor) {
        return model.accept(visitor);
    }

    /**
     * Get the property from the bean.
     *
     * @param bean The bean.
     * @return The property value.
     */
    public P get(B bean) {
        return bean == null ? null : getter.apply(bean);
    }

    /**
     * Set the property to the bean.
     *
     * @param bean  The bean.
     * @param value The property new value.
     */
    public void set(B bean, P value) {
        if (bean != null) {
            setter.accept(bean, value);
        }
    }

    /**
     * Get the property {@link Model}.
     *
     * @return The model.
     */
    public Model<P> getModel() {
        return model;
    }

    /**
     * Get the property name.
     *
     * @return The property name.
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        return this == that || that instanceof Property<?, ?> && Objects.equals(name, ((Property<?, ?>) that).name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
