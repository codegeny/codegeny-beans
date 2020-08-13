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
package org.codegeny.beans.diff.visitor;

import org.codegeny.beans.diff.BeanDiff;
import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.DiffVisitor;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.diff.SetDiff;
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.path.Path;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Extract a path from a diff.
 *
 * @param <T> The diff'ed type.
 * @author Xavier DURY
 */
public final class GetDiffVisitor<T> implements DiffVisitor<T, Diff<?>> {

    /**
     * The path elements iterator.
     */
    private final Iterator<?> path;

    /**
     * Constructor.
     *
     * @param path The path.
     */
    public GetDiffVisitor(Path<?> path) {
        this(path.iterator());
    }

    /**
     * Constructor.
     *
     * @param path The path elements iterator.
     */
    private GetDiffVisitor(Iterator<?> path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Diff<?> visitSet(SetDiff<T, E> setDiff) {
        return followNestedOrGetValue(setDiff, pathElement -> setDiff.getSet().stream().filter(d -> Objects.equals(d.getLeft(), pathElement) || Objects.equals(d.getRight(), pathElement)).findFirst().orElse(null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Diff<?> visitList(ListDiff<T, E> listDiff) {
        return followNestedOrGetValue(listDiff, pathElement -> listDiff.getList().get(((Number) pathElement).intValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Diff<?> visitMap(MapDiff<T, K, V> mapDiff) {
        return followNestedOrGetValue(mapDiff, pathElement -> mapDiff.getMap().entrySet().stream().filter(e -> Objects.equals(e.getKey().getLeft(), pathElement) || Objects.equals(e.getKey().getRight(), pathElement)).findFirst().map(Map.Entry::getValue).orElse(null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<?> visitSimple(SimpleDiff<T> simpleDiff) {
        return followNestedOrGetValue(simpleDiff, pathElement -> {
            throw new IllegalArgumentException("SimpleDiff must be terminal");
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<?> visitBean(BeanDiff<T> beanDiff) {
        return followNestedOrGetValue(beanDiff, pathElement -> beanDiff.getProperty((String) pathElement));
    }

    /**
     * Follow the next path element (if any) or else get the value.
     *
     * @param diff         The current diff.
     * @param nestedGetter A function which takes a key and return a diff.
     * @param <N>          The type of the nested element.
     * @return A diff.
     */
    private <N> Diff<?> followNestedOrGetValue(Diff<T> diff, Function<Object, Diff<N>> nestedGetter) {
        return path.hasNext() ? nestedGetter.apply(path.next()).accept(new GetDiffVisitor<>(path)) : diff;
    }
}
