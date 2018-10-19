package org.codegeny.beans.model;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class Properties {
	
	public static <T, E> Function<T, Set<E>> set(Function<T, Set<E>> getter, BiConsumer<T, E> adder) {
		return t -> set(getter.apply(t), e -> adder.accept(t, e));
	}
	
	public static <E> Set<E> set(Set<E> set, Consumer<E> adder) {
		return new AbstractSet<E>() {

			@Override
			public Iterator<E> iterator() {
				return set.iterator();
			}

			@Override
			public int size() {
				return set.size();
			}
			
			@Override
			public boolean add(E e) {
				adder.accept(e);
				return true;
			}
		};
	}
	
	public static <T, E> Function<T, List<E>> list(Function<T, List<E>> getter, Function<T, IntFunction<Consumer<E>>> adder) {
		return t -> list(getter.apply(t), i -> e -> adder.apply(t).apply(i).accept(e));
	}
	
	public static <E> List<E> list(List<E> list, IntFunction<Consumer<E>> adder) {
		return new AbstractList<E>() {

			@Override
			public E get(int index) {
				return list.get(index);
			}

			@Override
			public int size() {
				return list.size();
			}
			
			@Override
			public E set(int index, E element) { // or add ?
				adder.apply(index).accept(element);
				return element;
			}
		};
	}
		
	public static <K, V> Map<K, V> map(Map<K, V> map, BiConsumer<K, V> putter, Consumer<Object> remover) {
		return new AbstractMap<K, V>() {
			
			@Override
			public Set<Entry<K, V>> entrySet() {
				return map.entrySet();
			}
			
			@Override
			public V put(K key, V value) {
				putter.accept(key,  value);
				return value;
			}
			
			@Override
			public V remove(Object key) {
				remover.accept(key);
				return null;
			}
		};
	}
	
	public static <K, V> Map<K, V> map(Map<K, V> map) {
		return map(map, (k, v) ->  {
			throw new UnsupportedOperationException();
		}, k -> {
			throw new UnsupportedOperationException();
		});
	}
	
	private Properties() {
		throw new InternalError();
	}
}
