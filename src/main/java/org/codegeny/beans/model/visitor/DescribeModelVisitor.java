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

public final class DescribeModelVisitor<T> implements ModelVisitor<T, StringBuilder> {

    private final StringBuilder builder;
    private final String indent;

    public DescribeModelVisitor() {
        this(new StringBuilder(), "");
    }

    private DescribeModelVisitor(StringBuilder builder, String indent) {
        this.builder = builder;
        this.indent = indent;
    }

    public StringBuilder visitBean(BeanModel<T> bean) {
        return bean.getProperties().stream()
                .reduce(
                        this.builder.append("Bean[").append(bean.getType().getName()).append("] {"),
                        (b, p) -> p.accept(indented(b.append("\n").append(this.indent).append("  ").append(p.getName()).append(": "))),
                        (x, y) -> null
                )
                .append(bean.getProperties().isEmpty() ? "" : "\n").append(this.indent).append("}");
    }

    public <K, V> StringBuilder visitMap(MapModel<T, K, V> map) {
        return map.acceptValue(indented(map.acceptKey(indented(this.builder.append("Map<"))).append(", "))).append(">");
    }

    public StringBuilder visitValue(ValueModel<T> value) {
        return this.builder.append("Value[").append(value.getType().getName()).append("]");
    }

    public <E> StringBuilder visitSet(SetModel<T, E> values) {
        return values.acceptElement(same(this.builder.append("Set<"))).append(">");
    }

    public <E> StringBuilder visitList(ListModel<T, E> values) {
        return values.acceptElement(same(this.builder.append("List<"))).append(">");
    }

    private <S> DescribeModelVisitor<S> indented(StringBuilder builder) {
        return new DescribeModelVisitor<>(builder, indent.concat("  "));
    }

    private <S> DescribeModelVisitor<S> same(StringBuilder builder) {
        return new DescribeModelVisitor<>(builder, indent);
    }
}
