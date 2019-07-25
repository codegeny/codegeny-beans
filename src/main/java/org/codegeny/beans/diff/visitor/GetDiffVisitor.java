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

import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.DiffVisitor;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.path.Path;

import java.util.Iterator;
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
    public <E> Diff<?> visitList(ListDiff<T, E> list) {
        return followNestedOrGetValue(list, pathElement -> list.getList().get((Integer) pathElement));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public <K, V> Diff<?> visitMap(MapDiff<T, K, V> map) {
        return followNestedOrGetValue(map, pathElement -> map.getMap().get(pathElement));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<?> visitSimple(SimpleDiff<T> simple) {
        return followNestedOrGetValue(simple, pathElement -> {
            throw new IllegalArgumentException("SimpleDiff must be terminal");
        });
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
