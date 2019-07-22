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
import java.util.function.Function;

/**
 * Implementation of {@link ModelVisitor} which extracts a value from a {@link Model} based on a {@link Path}.
 *
 * @param <S> The path element type.
 * @param <T> The target.
 * @author Xavier DURY
 */
public final class GetModelVisitor<S, T> implements ModelVisitor<T, Object> {

    private final T current;
    private final Iterator<? extends S> path;
    private final Typer<S> typer;

    public GetModelVisitor(T current, Typer<S> typer, Path<S> path) {
        this(current, path.iterator(), typer);
    }

    private GetModelVisitor(T current, Iterator<? extends S> path, Typer<S> typer) {
        this.current = current;
        this.path = path;
        this.typer = typer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visitBean(BeanModel<T> bean) {
        return process(k -> visitProperty(bean.getProperty(typer.retype(Model.STRING, k))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Object visitMap(MapModel<T, K, V> map) {
        Map<K, V> m = map.toMap(current);
        return process(map.getValueModel(), map.getKeyModel(), m::get);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Object visitSet(SetModel<T, E> set) {
        Set<E> s = set.toSet(current);
        return process(set.getElementModel(), Model.INTEGER, v -> s.stream().skip(v).findAny().orElse(null));
//		return process(set.getElementModel(), set.getElementModel(), v -> s.stream().filter(isEqual(v)).findAny().orElse(null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Object visitList(ListModel<T, E> list) {
        List<E> l = list.toList(current);
        return process(list.getElementModel(), Model.INTEGER, l::get);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visitValue(ValueModel<T> value) {
        return process(p -> {
            throw new UnsupportedOperationException("Value object must be terminal");
        });
    }

    private <P> Object visitProperty(Property<? super T, P> property) {
        return property.accept(visitor(property.get(current)));
    }

    private Object process(Function<? super S, ?> p) {
        return path.hasNext() ? p.apply(path.next()) : current;
    }

    private <I, E> Object process(Model<E> e, Model<I> i, Function<I, E> f) {
        return process(k -> e.accept(visitor(f.apply(typer.retype(i, k)))));
    }

    private <Z> GetModelVisitor<S, Z> visitor(Z current) {
        return new GetModelVisitor<>(current, path, typer);
    }
}
