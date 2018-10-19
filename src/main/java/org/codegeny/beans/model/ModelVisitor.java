package org.codegeny.beans.model;

public interface ModelVisitor<T, R> {
	
	R visitBean(BeanModel<T> bean);
	
	<E> R visitList(ListModel<T, E> list);
	
	<K, V> R visitMap(MapModel<T, K, V> map);
	
	<E> R visitSet(SetModel<T, E> set);
	
	R visitValue(ValueModel<T> value);
}