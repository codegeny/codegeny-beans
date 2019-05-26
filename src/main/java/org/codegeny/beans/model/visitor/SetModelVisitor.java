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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.function.Predicate.isEqual;

public final class SetModelVisitor<S, T> implements ModelVisitor<T, Void> {

    private final T current;
    private final S valueToSet;
    private final Iterator<? extends S> path;
    private final Consumer<? super T> setter;
    private final Typer<S> typer;

    public SetModelVisitor(T current, S valueToSet, Typer<S> typer, Path<S> path) {
        this(current, valueToSet, path.iterator(), typer, a -> {
            throw new UnsupportedOperationException("Cannot set root object");
        });
    }

    private SetModelVisitor(T current, S valueToSet, Iterator<? extends S> path, Typer<S> typer, Consumer<? super T> setter) {
        this.current = current;
        this.valueToSet = valueToSet;
        this.path = path;
        this.typer = typer;
        this.setter = setter;
    }

    @Override
    public Void visitBean(BeanModel<T> bean) {
        return process(k -> visitProperty(bean.getProperty(typer.retype(Model.STRING, k))), setter, bean);
    }

    private <P> void visitProperty(Property<? super T, P> property) {
        property.accept(visitor(property.get(current), a -> property.set(current, a)));
    }

    @Override
    public <K, V> Void visitMap(MapModel<T, K, V> map) {
        Map<K, V> m = map.toMap(current);
        return process(map, map.getValueModel(), map.getKeyModel(), m::get, m::put, t -> {
            m.clear();
            m.putAll(map.toMap(t));
        });
    }

    @Override
    public <E> Void visitSet(SetModel<T, E> set) {
        Set<E> s = set.toSet(current);
        return process(set, set.getElementModel(), set.getElementModel(), v -> s.stream().filter(isEqual(v)).findAny().orElse(null), (a, b) -> s.add(a), t -> {
            s.clear();
            s.addAll(set.toSet(t));
        });
    }

    @Override
    public <E> Void visitList(ListModel<T, E> list) {
        List<E> l = list.toList(current);
        return process(list, list.getElementModel(), Model.INTEGER, l::get, l::set, t -> {
            l.clear();
            l.addAll(list.toList(t));
        });
    }

    @Override
    public Void visitValue(ValueModel<T> value) {
        return process(p -> {
            throw new UnsupportedOperationException("Value object must be terminal");
        }, setter, value);
    }

    private Void process(Consumer<? super S> p, Consumer<? super T> c, Model<? extends T> t) {
        if (path.hasNext()) {
            p.accept(path.next());
        } else {
            c.accept(typer.retype(t, valueToSet));
        }
        return null;
    }

    private <I, E> Void process(Model<? extends T> t, Model<E> e, Model<? extends I> i, Function<? super I, ? extends E> f, BiConsumer<? super I, ? super E> b, Consumer<? super T> s) {
        return process(k -> {
            I value = typer.retype(i, k);
            e.accept(visitor(f.apply(value), z -> b.accept(value, z)));
        }, s, t);
    }

    private <Z> SetModelVisitor<S, Z> visitor(Z current, Consumer<? super Z> setter) {
        return new SetModelVisitor<>(current, valueToSet, path, typer, setter);
    }
}
