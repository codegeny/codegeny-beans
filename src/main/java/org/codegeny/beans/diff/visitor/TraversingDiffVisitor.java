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
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.path.Path;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import static org.codegeny.beans.util.Utils.forEachIndexed;

/**
 * Visitor which will traverse the whole diff tree.
 *
 * @param <T> The diff'ed type.
 * @author Xavier DURY
 */
public final class TraversingDiffVisitor<T> implements DiffVisitor<T, Void> {

    /**
     * The current path.
     */
    private final Path<Object> path;

    /**
     * A predicate which receives the current path and the current diff and returns a boolean indicating if traversing should stop.
     */
    private final BiPredicate<? super Path<?>, ? super Diff<?>> processor;

    /**
     * Constructor.
     *
     * @param processor A predicate which receives the current path and the current diff and returns a boolean indicating if traversing should stop.
     */
    public TraversingDiffVisitor(BiPredicate<? super Path<?>, ? super Diff<?>> processor) {
        this(Path.root(), processor);
    }

    /**
     * Constructor.
     *
     * @param processor A consumer which receives the current path and the current diff.
     */
    public TraversingDiffVisitor(BiConsumer<? super Path<?>, ? super Diff<?>> processor) {
        this(Path.root(), processor);
    }

    /**
     * Constructor.
     *
     * @param path      The current path.
     * @param processor A predicate which receives the current path and the current diff and returns a boolean indicating if traversing should stop.
     */
    private TraversingDiffVisitor(Path<Object> path, BiPredicate<? super Path<?>, ? super Diff<?>> processor) {
        this.path = path;
        this.processor = processor;
    }

    /**
     * Constructor.
     *
     * @param path      The current path.
     * @param processor A consumer which receives the current path and the current diff.
     */
    private TraversingDiffVisitor(Path<Object> path, BiConsumer<? super Path<?>, ? super Diff<?>> processor) {
        this(path, (a, b) -> {
            processor.accept(a, b);
            return true;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> Void visitList(ListDiff<T, E> listDiff) {
        if (processor.test(path, listDiff)) {
            forEachIndexed(listDiff, (n, i) -> n.accept(newVisitor(path.append(i))));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Void visitMap(MapDiff<T, K, V> mapDiff) {
        if (processor.test(path, mapDiff)) {
            mapDiff.forEach((k, v) -> v.accept(newVisitor(path.append(k))));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitSimple(SimpleDiff<T> simpleDiff) {
        processor.test(path, simpleDiff);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitBean(BeanDiff<T> beanDiff) {
        if (processor.test(path, beanDiff)) {
            beanDiff.forEach((k, v) -> v.accept(newVisitor(path.append(k))));
        }
        return null;
    }

    /**
     * Create a new visitor.
     *
     * @param path The current path.
     * @param <R>  The type.
     * @return A new visitor.
     */
    private <R> TraversingDiffVisitor<R> newVisitor(Path<Object> path) {
        return new TraversingDiffVisitor<>(path, processor);
    }
}
