package org.codegeny.beans.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public interface Path<P> extends Iterable<P> {
	
	static <P> Path<P> root() {
		return of();
	}
	
	@SafeVarargs
	static <P> Path<P> of(P... pathElements) {
		return of(Arrays.asList(pathElements));
	}
	
	static <P> Path<P> of(List<P> pathElements) {
		return new ArrayList<>(pathElements)::iterator;
	}
	
	default Path<P> append(P pathElement) {
		List<P> result = new LinkedList<>();
		forEach(result::add);
		result.add(pathElement);
		return of(result);
	}
	
	default Path<P> prepend(P pathElement) {
		List<P> result = new LinkedList<>();
		result.add(pathElement);
		forEach(result::add);
		return of(result);
	}
}
