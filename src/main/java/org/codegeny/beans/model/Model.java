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

import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.model.visitor.CompareModelVisitor;
import org.codegeny.beans.model.visitor.ComputeDiffModelVisitor;
import org.codegeny.beans.model.visitor.DescribeModelVisitor;
import org.codegeny.beans.model.visitor.GetModelVisitor;
import org.codegeny.beans.model.visitor.SetModelVisitor;
import org.codegeny.beans.model.visitor.ToStringModelVisitor;
import org.codegeny.beans.path.Converter;
import org.codegeny.beans.path.Path;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;

/**
 * Base interface for modeling an object structure as a tree.
 * Models can be viewed as nodes from a tree.
 * A model by itself cannot do much but it can be visited by {@link ModelVisitor}s which can implement complex logic.
 * There are currently different type of models/nodes:
 * <ul>
 * <li>{@link BeanModel} which represents a bean.</li>
 * <li>{@link ValueModel} which represents a terminal value (leaf). Generally atomic types like Numbers, Strings or simple value objects.</li>
 * <li>{@link SetModel} which represents a set of objects.</li>
 * <li>{@link ListModel} which represents a list of objects.</li>
 * <li>{@link MapModel} which represents a map.</li>
 * </ul>
 * All implementations are required to be thread-safe.
 *
 * @param <T> The type of object this model represents.
 * @author Xavier DURY
 */
public interface Model<T> extends Comparator<T> {

    /**
     * A {@link ValueModel} for {@link Boolean}s.
     */
    Model<Boolean> BOOLEAN = value(Boolean.class);

    /**
     * A {@link ValueModel} for {@link Byte}s.
     */
    Model<Byte> BYTE = value(Byte.class);

    /**
     * A {@link ValueModel} for {@link Short}s.
     */
    Model<Short> SHORT = value(Short.class);

    /**
     * A {@link ValueModel} for {@link Integer}s.
     */
    Model<Integer> INTEGER = value(Integer.class);

    /**
     * A {@link ValueModel} for {@link Long}s.
     */
    Model<Long> LONG = value(Long.class);

    /**
     * A {@link ValueModel} for {@link Float}s.
     */
    Model<Float> FLOAT = value(Float.class);

    /**
     * A {@link ValueModel} for {@link Double}s.
     */
    Model<Double> DOUBLE = value(Double.class);

    /**
     * A {@link ValueModel} for {@link Character}s.
     */
    Model<Character> CHARACTER = value(Character.class);

    /**
     * A {@link ValueModel} for {@link String}s.
     */
    Model<String> STRING = value(String.class);

    /**
     * Construct a new empty {@link BeanModel}.
     *
     * @param type       The bean class.
     * @param properties The bean properties.
     * @param <B>        The bean type.
     * @return The bean model.
     */
    @SafeVarargs
    static <B> BeanModel<B> bean(Class<? extends B> type, Property<? super B, ?>... properties) {
        return new BeanModel<>(type, properties);
    }

    /**
     * Construct a new {@link ListModel} for a list of &lt;E&gt; elements which implements the {@link List} interface.
     *
     * @param elementModel The delegate {@link Model} to be used for elements.
     * @param <E>          The type of elements.
     * @return The list model.
     */
    static <E> ListModel<List<E>, E> list(Model<E> elementModel) {
        return list(elementModel, identity());
    }

    /**
     * Construct a new {@link ListModel} for a list of &lt;E&gt; objects which does not implement the {@link List} interface.
     *
     * @param elementModel The delegate {@link Model} to be used for elements.
     * @param converter    The converter is a function which transforms objects of type &lt;L&gt; to a <code>List&lt;E&gt;</code>.
     * @param <L>          The type of the list of &lt;E&gt; elements.
     * @param <E>          The type of elements.
     * @return The list model.
     */
    static <L, E> ListModel<L, E> list(Model<E> elementModel, Function<? super L, ? extends List<E>> converter) {
        return new ListModel<>(elementModel, converter);
    }

    /**
     * Construct a new {@link MapModel} for a map of &lt;K, V&gt; entries which implements the {@link Map} interface.
     *
     * @param keyModel   The delegate {@link Model} to be used for keys.
     * @param valueModel The delegate {@link Model} to be used for values.
     * @param <K>        The type of keys.
     * @param <V>        The type of values.
     * @return The map model.
     */
    static <K, V> MapModel<Map<K, V>, K, V> map(Model<K> keyModel, Model<V> valueModel) {
        return map(keyModel, valueModel, identity());
    }

    /**
     * Construct a new {@link MapModel} for a map of &lt;K, V&gt; entries which does not implement the {@link Map} interface.
     *
     * @param keyModel   The delegate {@link Model} to be used for keys.
     * @param valueModel The delegate {@link Model} to be used for values.
     * @param converter  The collector is a function which transforms objects of type &lt;M&gt; to a <code>Map&lt;K, V&gt;</code>.
     * @param <M>        The type of the map of &lt;K, V&gt; entries.
     * @param <K>        The type of keys.
     * @param <V>        The type of values.
     * @return The map model.
     */
    static <M, K, V> MapModel<M, K, V> map(Model<K> keyModel, Model<V> valueModel, Function<? super M, ? extends Map<K, V>> converter) {
        return new MapModel<>(keyModel, valueModel, converter);
    }

    /**
     * Construct a new {@link SetModel} for a set of &lt;E&gt; elements which implements the {@link Set} interface.
     *
     * @param elementModel The delegate {@link Model} to be used for elements.
     * @param <E>          The type of elements.
     * @return The set model.
     */
    static <E> SetModel<Set<E>, E> set(Model<E> elementModel) {
        return set(elementModel, identity());
    }

    /**
     * Construct a new {@link SetModel} for a set of &lt;E&gt; objects which does not implement the {@link Set} interface.
     *
     * @param elementModel The delegate {@link Model} to be used for elements.
     * @param converter    The collector is a function which transforms objects of type &lt;S&gt; to a <code>Collection&lt;E&gt;</code>.
     * @param <S>          The type of the set of &lt;E&gt; elements.
     * @param <E>          The type of elements.
     * @return The set model.
     */
    static <S, E> SetModel<S, E> set(Model<E> elementModel, Function<? super S, ? extends Set<E>> converter) {
        return new SetModel<>(elementModel, converter);
    }

    /**
     * Construct a new {@link ValueModel} for an object which implements the {@link Comparable} interface.
     *
     * @param <V> The type of the value which must be comparable.
     * @return The value model.
     */
    static <V extends Comparable<? super V>> ValueModel<V> value(Class<? extends V> type) {
        return value(type, naturalOrder());
    }

    /**
     * Construct a new {@link ValueModel} for an object which does not implement the {@link Comparable} interface.
     *
     * @param comparator The {@link Comparator} to be used to compare objects of type &lt;T&gt;
     * @param <V>        The type of the value.
     * @return The value model.
     */
    static <V> ValueModel<V> value(Class<? extends V> type, Comparator<? super V> comparator) {
        return new ValueModel<>(type, comparator);
    }

    /**
     * Construct a property to be used for beans.
     *
     * @param name   The property name.
     * @param getter The property extractor/getter.
     * @param setter The property mutator/Setter.
     * @param model  The property model.
     * @param <B>    The bean type.
     * @param <P>    The property type.
     * @return A property.
     */
    static <B, P> Property<B, P> property(String name, Function<? super B, ? extends P> getter, BiConsumer<? super B, ? super P> setter, Model<P> model) {
        return new Property<>(name, getter, setter, model);
    }

    /**
     * Construct a read-only property to be used for beans.
     *
     * @param name   The property name.
     * @param getter The property extractor/getter.
     * @param model  The property model.
     * @param <B>    The bean type.
     * @param <P>    The property type.
     * @return A property.
     */
    static <B, P> Property<B, P> property(String name, Function<? super B, ? extends P> getter, Model<P> model) {
        return property(name, getter, (b, p) -> {
            throw new UnsupportedOperationException(String.format("Property '%s' is read-only", name));
        }, model);
    }

    /**
     * Accept a {@link ModelVisitor} and return some result.
     *
     * @param visitor The model visitor.
     * @param <R>     The type of the result.
     * @return The result of the visitor computation.
     */
    <R> R accept(ModelVisitor<T, ? extends R> visitor);

    /**
     * {@inheritDoc}
     */
    @Override
    default int compare(T left, T right) {
        return accept(new CompareModelVisitor<>(left, right));
    }

    /**
     * Create a string representation of the model.
     *
     * @return A string representation for the model.
     */
    default String describe() {
        return accept(new DescribeModelVisitor<>()).toString();
    }

    /**
     * Set the value at the specified path in the target.
     * A typer is needed if, for example, you express your path as a sequence of <code>String</code>s but you need
     * <code>Integer</code>s for lists indexes or any other types for maps keys. The value to set could also be given
     * as a <code>String</code> and converted at the last moment (when it is actually set on the target object).
     *
     * @param target    The &gt;T&lt; instance to modify.
     * @param path      The path of the property to set.
     * @param value     The value to set.
     * @param converter The converter for converting path elements along the way.
     * @param <S>       The bottom type for path elements and value.
     */
    default <S> void set(T target, Path<S> path, S value, Converter<? super S> converter) {
        accept(new SetModelVisitor<>(target, value, path, converter));
    }

    /**
     * Get the value from the specified path in the target.
     * A typer is needed for the same reasons as for {@link Model#set(Object, Path, Object, Converter)}.
     *
     * @param target    The &gt;T&lt; instance.
     * @param path      The path of the property to retrieve.
     * @param converter The converter for converting path elements along the way.
     * @param <S>       The bottom type for path elements.
     * @return The retrieved property value.
     */
    default <S> Object get(T target, Path<S> path, Converter<? super S> converter) {
        return accept(new GetModelVisitor<>(target, path, converter));
    }

    /**
     * Set the value at the specified path in the target.
     * No typer is needed here as path elements and value are expected to be already of the correct type
     * (no conversion needed).
     *
     * @param target The &gt;T&lt; instance to modify.
     * @param path   The path of the property to set.
     * @param value  The value to set.
     */
    default <S> void set(T target, Path<S> path, S value) {
        set(target, path, value, Converter.Identity.INSTANCE);
    }

    /**
     * Get the value from the specified path in the target.
     * No typer is needed here as path elements are expected to be already of the correct type (no conversion needed).
     *
     * @param target The &gt;T&lt; instance.
     * @param path   The path of the property to retrieve.
     * @return The retrieved property value.
     */
    default <S> Object get(T target, Path<S> path) {
        return get(target, path, Converter.Identity.INSTANCE);
    }

    /**
     * Create a string representation for the given target.
     *
     * @param target The target object.
     * @return A string representation for that object.
     */
    default String toString(T target) {
        return accept(new ToStringModelVisitor<>(target)).toString();
    }

    /**
     * Diff two values.
     *
     * @param left  The left value.
     * @param right The right value.
     * @return The diff.
     */
    default Diff<T> diff(T left, T right) {
        return accept(new ComputeDiffModelVisitor<>(left, right));
    }
}
