package org.codegeny.beans.path;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class Path<P> implements Iterable<P> {
	
	private final List<P> elements;
	
	private Path(List<P> elements) {
		this.elements = Collections.unmodifiableList(elements);
	}

	public static <P> Path<P> root() {
		return of();
	}
	
	@SafeVarargs
	public static <P> Path<P> of(P... elements) {
		return of(Arrays.asList(elements));
	}
	
	public static <P> Path<P> of(List<P> elements) {
		return new Path<>(new ArrayList<>(elements));
	}
	
	public Path<P> append(P element) {
		List<P> result = new ArrayList<>(size() + 1);
		forEach(result::add);
		result.add(element);
		return new Path<>(result);
	}
	
	public Path<P> prepend(P element) {
		List<P> result = new ArrayList<>(size() + 1);
		result.add(element);
		forEach(result::add);
		return new Path<>(result);
	}

	public Path<P> append(Path<P> path) {
		List<P> result = new ArrayList<>(size() + path.size());
		forEach(result::add);
		path.forEach(result::add);
		return new Path<>(result);
	}
	
	public Path<P> prepend(Path<P> path) {
		List<P> result = new ArrayList<>(size() + path.size());
		path.forEach(result::add);
		forEach(result::add);
		return new Path<>(result);
	}
	
	public int size() {
		return elements.size();
	}

	@Override
	public Iterator<P> iterator() {
		return elements.iterator();
	}
	
	public String toString(String separator, String prefix, String suffix) {
		return elements.stream().map(Object::toString).collect(Collectors.joining(separator, prefix, suffix));
	}
	
	@Override
	public String toString() {
		return toString("/", "/", "");
	}
	
	@Override
	public int hashCode() {
		return elements.hashCode();
	}
	
	@Override
	public boolean equals(Object that) {
		return super.equals(that) || that instanceof Path && ((Path<?>) that).elements.equals(this.elements);
	}
}
