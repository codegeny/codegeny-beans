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

import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Implementation of <code>{@link Diff}</code> for sets.
 *
 * @param <L> The type of set.
 * @param <E> The type of set element.
 * @author Xavier DURY
 */
public final class SetDiff<L, E> extends Diff<L> {

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The set of diffs.
     */
    private final Set<Diff<E>> set;

    /**
     * Constructor.
     *
     * @param status The status.
     * @param left   The left value.
     * @param right  The right value.
     * @param set    The set of diffs.
     */
    SetDiff(Status status, L left, L right, Set<? extends Diff<E>> set) {
        super(status, left, right);
        this.set = unmodifiableSet(set);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> R accept(DiffVisitor<L, R> visitor) {
        return visitor.visitSet(this);
    }

    /**
     * Return the set of diffs.
     *
     * @return The set of diffs.
     */
    public Set<Diff<E>> getSet() {
        return set;
    }
}
