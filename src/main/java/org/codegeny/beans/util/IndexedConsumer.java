package org.codegeny.beans.util;

import java.util.Collection;

import org.codegeny.beans.util.IndexedConsumer;

public interface IndexedConsumer<T> {

	void accept(int index, T target);

	static <T> int forEachIndexed(Collection<T> collection, IndexedConsumer<T> consumer) {
		int index = 0;
		for (T element : collection) {
			consumer.accept(index++, element);
		}
		return index;
	}
}