package org.codegeny.beans.model;

public interface ModelVisitor<T, R> {
	
	R visitBean(BeanModel<? super T> bean);
	
	<E> R visitList(ListModel<? super T, E> list);
	
	<K, V> R visitMap(MapModel<? super T, K, V> map);
	
	<E> R visitSet(SetModel<? super T, E> set);
	
	R visitValue(ValueModel<? super T> value);
}