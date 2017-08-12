package org.codegeny.beans.util;

public interface IndexedConsumer<T> {

	void accept(int index, T target);

	static <T> int forEachIndexed(Iterable<? extends T> collection, IndexedConsumer<? super T> consumer) {
		int index = 0;
		for (T element : collection) {
			consumer.accept(index++, element);
		}
		return index;
	}
}