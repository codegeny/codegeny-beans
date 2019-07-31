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
package org.codegeny.beans.diff;

import java.util.AbstractList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of <code>{@link Diff}</code> for lists.
 *
 * @param <L> The type of list.
 * @param <E> The type of list element.
 * @author Xavier DURY
 */
public final class ListDiff<L, E> extends AbstractList<Diff<E>> implements Diff<L> {

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The left value.
     */
    private final L left;

    /**
     * The right value.
     */
    private final L right;

    /**
     * The score.
     */
    private final double normalizedScore;

    /**
     * The status.
     */
    private final Status status;

    /**
     * The list of diffs.
     */
    private final List<Diff<E>> list;

    /**
     * Constructor.
     *
     * @param status The status.
     * @param left   The left value.
     * @param right  The right value.
     * @param list   The list of diffs.
     */
    ListDiff(Status status, L left, L right, List<? extends Diff<E>> list) {
        this.normalizedScore = status.isChanged() ? list.stream().mapToDouble(Diff::getScore).average().orElse(0.0) : 1.0;
        this.status = requireNonNull(status, "Status cannot be null");
        this.left = left;
        this.right = right;
        this.list = unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(DiffVisitor<L, R> visitor) {
        return visitor.visitList(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public L getLeft() {
        return left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public L getRight() {
        return right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getScore() {
        return normalizedScore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Diff<E> get(int index) {
        return list.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return list.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ListDiff{" +
                "left=" + left +
                ", right=" + right +
                ", score=" + normalizedScore +
                ", status=" + status +
                '}';
    }
}
