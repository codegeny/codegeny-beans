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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.codegeny.beans.util.Utils.forEachIndexed;

/**
 * TODO
 *
 * @param <T> TODO
 * @author Xavier DURY
 */
public final class ToStringModelVisitor<T> implements ModelVisitor<T, StringBuilder> {

    private final StringBuilder builder;
    private final String indent;
    private final T target;

    public ToStringModelVisitor(T target) {
        this(target, new StringBuilder(), "");
    }

    private ToStringModelVisitor(T target, StringBuilder builder, String indent) {
        this.target = target;
        this.builder = builder;
        this.indent = indent;
    }

    @Override
    public StringBuilder visitBean(BeanModel<T> bean) {
        this.builder.append("{");
        int count = forEachIndexed(bean.getProperties(), (p, i) -> {
            this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ").append(p.getName()).append(": ");
            visitProperty(p);
        });
        return this.builder.append(count > 0 ? "\n" : "").append(this.indent).append("}");
    }

    @Override
    public StringBuilder visitValue(ValueModel<T> value) {
        return this.builder.append(this.target);
    }

    @Override
    public <K, V> StringBuilder visitMap(MapModel<T, K, V> map) {
        this.builder.append("[");
        Comparator<? super K> comparator = map.getKeyModel();
        Map<? extends K, ? extends V> entries = map.toMap(this.target);
        Collection<K> sorted = entries.keySet().stream().sorted(comparator).collect(toList());
        int count = forEachIndexed(sorted, (v, i) -> map.acceptValue(new ToStringModelVisitor<>(entries.get(v), this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ").append(v).append(": "), this.indent.concat("  "))));
        return this.builder.append(count > 0 ? "\n".concat(this.indent) : "").append("]");
    }

    @Override
    public <E> StringBuilder visitSet(SetModel<T, E> values) {
        return visitCollection(values.getElementModel(), values.toSet(this.target).stream().sorted(values.getElementModel()).collect(toList()));
    }

    @Override
    public <E> StringBuilder visitList(ListModel<T, E> values) {
        return visitCollection(values.getElementModel(), values.toList(this.target));
    }

    private <E> StringBuilder visitCollection(Model<E> elementModel, Collection<? extends E> list) {
        this.builder.append("[");
        int count = forEachIndexed(list, (v, i) -> elementModel.accept(new ToStringModelVisitor<>(v, this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  "), this.indent.concat("  "))));
        return this.builder.append(count > 0 ? "\n".concat(this.indent) : "").append("]");
    }

    private <P> void visitProperty(Property<? super T, P> property) {
        property.accept(new ToStringModelVisitor<>(property.get(this.target), this.builder, this.indent.concat("  ")));
    }
}
