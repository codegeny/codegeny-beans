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

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

public final class PathToModelVisitor<S, T> implements ModelVisitor<T, Model<?>> {

    private final Iterator<? extends S> path;
    private final Converter<S> converter;

    @SafeVarargs
    public PathToModelVisitor(Converter<S> typer, S... path) {
        this(Arrays.asList(path).iterator(), typer);
    }

    private PathToModelVisitor(Iterator<? extends S> path, Converter<S> converter) {
        this.path = path;
        this.converter = converter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model<?> visitBean(BeanModel<T> bean) {
        return process(bean, k -> visitProperty(bean.getProperty(converter.convert(String.class, k))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Model<?> visitMap(MapModel<T, K, V> map) {
        return process2(map, map.getValueModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Model<?> visitSet(SetModel<T, E> set) {
        return process2(set, set.getElementModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Model<?> visitList(ListModel<T, E> list) {
        return process2(list, list.getElementModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model<?> visitValue(ValueModel<T> value) {
        return process(value, p -> {
            throw new UnsupportedOperationException("Value object must be terminal");
        });
    }

    private <P> Model<?> visitProperty(Property<? super T, P> property) {
        return property.accept(visitor());
    }

    private Model<?> process(Model<?> model, Function<? super S, Model<?>> p) {
        return path.hasNext() ? p.apply(path.next()) : model;
    }

    private <E> Model<?> process2(Model<T> t, Model<E> e) {
        return process(t, k -> e.accept(visitor()));
    }

    private <Z> PathToModelVisitor<S, Z> visitor() {
        return new PathToModelVisitor<>(path, converter);
    }
}
