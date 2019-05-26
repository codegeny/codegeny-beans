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

import org.codegeny.beans.model.*;
import org.codegeny.beans.path.Path;

import java.util.function.BiConsumer;

import static org.codegeny.beans.util.Utils.forEachIndexed;

/**
 * TODO
 *
 * @param <T> TODO
 * @author Xavier DURY
 */
public final class TraversingModelVisitor<T> implements ModelVisitor<T, Void> {

    private final Path<Object> path;
    private final BiConsumer<? super Path<?>, Object> processor;
    private final T target;

    public TraversingModelVisitor(T target, BiConsumer<? super Path<?>, Object> processor) {
        this(target, Path.root(), processor);
    }

    private TraversingModelVisitor(T target, Path<Object> path, BiConsumer<? super Path<?>, Object> processor) {
        this.target = target;
        this.path = path;
        this.processor = processor;
    }

    private <R> TraversingModelVisitor<R> childVisitor(R target, Path<Object> path) {
        return new TraversingModelVisitor<>(target, path, processor);
    }

    private void process() {
        this.processor.accept(path, target);
    }

    @Override
    public Void visitBean(BeanModel<T> bean) {
        process();
        bean.getProperties().forEach(this::visitProperty);
        return null;
    }

    @Override
    public <E> Void visitList(ListModel<T, E> list) {
        process();
        forEachIndexed(list.toList(target), (n, i) -> list.acceptElement(childVisitor(n, path.append(i))));
        return null;
    }

    @Override
    public <K, V> Void visitMap(MapModel<T, K, V> map) {
        process();
        map.toMap(target).forEach((k, v) -> map.acceptValue(childVisitor(v, path.append(k))));
        return null;
    }

    private <P> void visitProperty(Property<? super T, P> property) {
        property.accept(childVisitor(property.get(target), path.append(property.getName())));
    }

    @Override
    public <E> Void visitSet(SetModel<T, E> set) {
        process();
        forEachIndexed(set.toSet(target), (n, i) -> set.acceptElement(childVisitor(n, path.append(i))));
        return null;
    }

    @Override
    public Void visitValue(ValueModel<T> value) {
        process();
        return null;
    }
}
