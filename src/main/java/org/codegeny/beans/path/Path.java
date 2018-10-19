package org.codegeny.beans.path;

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
		List<P> result = new ArrayList<>(elements.size() + 1);
		forEach(result::add);
		result.add(element);
		return new Path<>(result);
	}
	
	public Path<P> prepend(P element) {
		List<P> result = new ArrayList<>(elements.size() + 1);
		result.add(element);
		forEach(result::add);
		return new Path<>(result);
	}
	
	@Override
	public Iterator<P> iterator() {
		return elements.iterator();
	}
	
	@Override
	public String toString() {
		return elements.stream().map(Object::toString).collect(Collectors.joining("/", "/", ""));
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
