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
package org.codegeny.beans.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * A path made of separate elements.
 *
 * @param <P> The type of the path elements.
 * @author Xavier DURY
 */
public final class Path<P> implements Iterable<P> {

    /**
     * Construct an empty path.
     *
     * @param <P> The type of the path elements.
     * @return A path.
     */
    public static <P> Path<P> root() {
        return of();
    }

    /**
     * Construct a path with the given elements.
     *
     * @param elements The path elements.
     * @param <P>      The type of the path elements.
     * @return A path.
     */
    @SafeVarargs
    public static <P> Path<P> of(P... elements) {
        return of(Arrays.asList(elements));
    }

    /**
     * Construct a path with the given elements.
     *
     * @param elements The path elements.
     * @param <P>      The type of the path elements.
     * @return A path.
     */
    public static <P> Path<P> of(List<P> elements) {
        return new Path<>(new ArrayList<>(elements));
    }

    /**
     * The path elements.
     */
    private final List<P> elements;

    /**
     * Constructor.
     *
     * @param elements The path elements.
     */
    private Path(List<P> elements) {
        this.elements = Collections.unmodifiableList(elements);
    }

    /**
     * Append an element to this path.
     *
     * @param element The element to append.
     * @return A new path with the appended element.
     */
    public Path<P> append(P element) {
        List<P> result = new ArrayList<>(elements);
        result.add(element);
        return new Path<>(result);
    }

    /**
     * Prepend an element to this path.
     *
     * @param element The element to prepend.
     * @return A new path with the prepended element.
     */
    public Path<P> prepend(P element) {
        List<P> result = new ArrayList<>(elements);
        result.add(0, element);
        return new Path<>(result);
    }

    /**
     * Append a path to this path.
     *
     * @param path The path to append.
     * @return A new path with the appended path.
     */
    public Path<P> append(Path<P> path) {
        List<P> result = new ArrayList<>(elements);
        result.addAll(path.elements);
        return new Path<>(result);
    }

    /**
     * Prepend a path to this path.
     *
     * @param path The path to prepend.
     * @return A new path with the prepended path.
     */
    public Path<P> prepend(Path<P> path) {
        List<P> result = new ArrayList<>(path.elements);
        result.addAll(elements);
        return new Path<>(result);
    }

    /**
     * Get the number of path elements.
     *
     * @return The path size.
     */
    private int size() {
        return elements.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<P> iterator() {
        return elements.iterator();
    }

    /**
     * Create a string representation of this path.
     *
     * @param separator The separator between each element.
     * @param prefix    The prefix.
     * @param suffix    The suffix.
     * @return A string representation.
     */
    public String toString(String separator, String prefix, String suffix) {
        return elements.stream().map(Object::toString).collect(joining(separator, prefix, suffix));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString("/", "/", "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        return super.equals(that) || that instanceof Path && ((Path<?>) that).elements.equals(elements);
    }
}
