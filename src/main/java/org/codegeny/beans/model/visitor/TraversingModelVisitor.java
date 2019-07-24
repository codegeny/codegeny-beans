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
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;
import org.codegeny.beans.path.Path;

import java.util.function.BiConsumer;

import static org.codegeny.beans.util.Utils.forEachIndexed;

/**
 * Visitor which will traverse the whole model tree for a given instance of &gt;T&lt;.
 *
 * @param <T> The model type.
 * @author Xavier DURY
 */
public final class TraversingModelVisitor<T> implements ModelVisitor<T, Void> {

    /**
     * The current path.
     */
    private final Path<Object> path;

    /*
     * A consumer which receives the current path and the current diff.
     */
    private final BiConsumer<? super Path<?>, Object> processor;

    /**
     * The current value.
     */
    private final T target;

    /**
     * Constructor.
     *
     * @param target    The root value.
     * @param processor A consumer which receives the current path and the current diff.
     */
    public TraversingModelVisitor(T target, BiConsumer<? super Path<?>, Object> processor) {
        this(target, Path.root(), processor);
    }

    /**
     * Constructor.
     *
     * @param target    The current value.
     * @param path      The current path.
     * @param processor A consumer which receives the current path and the current diff.
     */
    private TraversingModelVisitor(T target, Path<Object> path, BiConsumer<? super Path<?>, Object> processor) {
        this.target = target;
        this.path = path;
        this.processor = processor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitBean(BeanModel<T> bean) {
        process();
        bean.getProperties().forEach(this::visitProperty);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Void visitList(ListModel<T, E> list) {
        process();
        forEachIndexed(list.toList(target), (n, i) -> list.acceptElement(newVisitor(n, path.append(i))));
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Void visitMap(MapModel<T, K, V> map) {
        process();
        map.toMap(target).forEach((k, v) -> map.acceptValue(newVisitor(v, path.append(k))));
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Void visitSet(SetModel<T, E> set) {
        process();
        forEachIndexed(set.toSet(target), (n, i) -> set.acceptElement(newVisitor(n, path.append(i))));
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitValue(ValueModel<T> value) {
        process();
        return null;
    }

    /**
     * Visit a property.
     *
     * @param property The property.
     * @param <P>      The property type.
     */
    private <P> void visitProperty(Property<? super T, P> property) {
        property.accept(newVisitor(property.get(target), path.append(property.getName())));
    }

    /**
     * Process the current path and value.
     */
    private void process() {
        processor.accept(path, target);
    }

    /**
     * Create a new visitor.
     *
     * @param target The value.
     * @param path   The path.
     * @param <R>    The value type.
     * @return A new visitor.
     */
    private <R> TraversingModelVisitor<R> newVisitor(R target, Path<Object> path) {
        return new TraversingModelVisitor<>(target, path, processor);
    }
}
