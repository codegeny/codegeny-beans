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
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

/**
 * Create a recursive string description of the given model.
 *
 * @param <T> The model type.
 * @author Xavier DURY
 */
public final class DescribeModelVisitor<T> implements ModelVisitor<T, StringBuilder> {

    /**
     * The string builder.
     */
    private final StringBuilder builder;

    /**
     * The string indentation.
     */
    private final String indent;

    /**
     * Constructor.
     */
    public DescribeModelVisitor() {
        this(new StringBuilder(), "");
    }

    /**
     * Constructor.
     *
     * @param builder The string builder.
     * @param indent  The string indentation.
     */
    private DescribeModelVisitor(StringBuilder builder, String indent) {
        this.builder = builder;
        this.indent = indent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder visitBean(BeanModel<T> bean) {
        return bean.getProperties().stream()
                .reduce(
                        builder.append("Bean[").append(bean.getType().getName()).append("] {"),
                        (b, p) -> p.accept(indented(b.append("\n").append(indent).append("  ").append(p.getName()).append(": "))),
                        (x, y) -> null
                )
                .append(bean.getProperties().isEmpty() ? "" : "\n").append(indent).append("}");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> StringBuilder visitMap(MapModel<T, K, V> map) {
        return map.acceptValue(indented(map.acceptKey(indented(builder.append("Map<"))).append(", "))).append(">");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuilder visitValue(ValueModel<T> value) {
        return builder.append("Value[").append(value.getType().getName()).append("]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> StringBuilder visitSet(SetModel<T, E> values) {
        return values.acceptElement(same(builder.append("Set<"))).append(">");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> StringBuilder visitList(ListModel<T, E> values) {
        return values.acceptElement(same(builder.append("List<"))).append(">");
    }

    /**
     * Create a new indented {@link DescribeModelVisitor} for <code>S</code>.
     *
     * @param builder The string builder.
     * @param <S>     The target type of the new visitor.
     * @return A new visitor.
     */
    private <S> DescribeModelVisitor<S> indented(StringBuilder builder) {
        return new DescribeModelVisitor<>(builder, indent.concat("  "));
    }

    /**
     * Create a new non-indented {@link DescribeModelVisitor} for <code>S</code>.
     *
     * @param builder The string builder.
     * @param <S>     The target type of the new visitor.
     * @return A new visitor.
     */
    private <S> DescribeModelVisitor<S> same(StringBuilder builder) {
        return new DescribeModelVisitor<>(builder, indent);
    }
}
