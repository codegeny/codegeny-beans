package org.codegeny.beans.path;

import java.io.Serializable;
import java.util.stream.Stream;

import org.codegeny.beans.path.visitor.ToStringPathVisitor;

public interface Path extends Serializable {

	static Path path() {
		return Stream::empty;
	}
	
	default <R> R accept(R parent, PathVisitor<R> visitor) {
		return elements().reduce(parent, (p, e) -> e.accept(p, visitor), (a, b) -> b);
	}
	
	default Path append(Path path) {
		return () -> Stream.concat(elements(), path.elements());
	}
		
	default Path append(PathElement element) {
		return () -> Stream.concat(elements(), Stream.of(element));
	}
	
	Stream<PathElement> elements();

	default Path index(int index) {
		return append(new IndexPathElement(index));
	}

	default Path key(Object key) {
		return append(new KeyPathElement(key));
	}

	default Path property(String property) {
		return append(new PropertyPathElement(property));
	}
	
	default String toString(String root) {
		return accept(new StringBuilder(root), new ToStringPathVisitor()).toString();
	}
}
