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

import org.codegeny.beans.hash.Hasher;
import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public final class HashModelVisitor<T> implements ModelVisitor<T, Hasher> {

    private final Hasher hasher;
    private final T target;

    public HashModelVisitor(T target, Hasher hasher) {
        this.target = target;
        this.hasher = hasher;
    }

    @Override
    public Hasher visitBean(BeanModel<T> bean) {
        return bean.getProperties().stream().reduce(this.hasher, this::visitProperty, (x, y) -> null);
    }

    @Override
    public Hasher visitValue(ValueModel<T> value) {
        return this.hasher.hash(this.target);
    }

    @Override
    public <K, V> Hasher visitMap(MapModel<T, K, V> map) {
        return map.toMap(this.target).entrySet().stream().reduce(this.hasher, (h, e) -> map.acceptKey(new HashModelVisitor<>(e.getKey(), map.acceptValue(new HashModelVisitor<>(e.getValue(), h)))), (x, y) -> null);
    }

    @Override
    public <E> Hasher visitSet(SetModel<T, E> values) {
        return values.toSet(this.target).stream().reduce(this.hasher, (h, e) -> values.acceptElement(new HashModelVisitor<>(e, h)), (x, y) -> null);
    }

    @Override
    public <E> Hasher visitList(ListModel<T, E> values) {
        return values.toList(this.target).stream().reduce(this.hasher, (h, e) -> values.acceptElement(new HashModelVisitor<>(e, h)), (x, y) -> null);
    }

    private <P> Hasher visitProperty(Hasher hasher, Property<? super T, P> property) {
        return property.accept(new HashModelVisitor<>(property.get(target), hasher));
    }
}
