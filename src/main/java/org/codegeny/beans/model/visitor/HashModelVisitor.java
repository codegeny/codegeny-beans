package org.codegeny.beans.model.visitor;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;
import org.codegeny.beans.util.Hasher;

public class HashModelVisitor<T> implements ModelVisitor<T, Hasher> {
	
	private final Hasher hasher;
	private final T target;

	public HashModelVisitor(T target, Hasher hasher) {
		this.target = target;
		this.hasher = hasher;
	}

	public Hasher visitBean(BeanModel<T> bean) {
		bean.forEach(this::visitProperty);
		return hasher;
	}

	public Hasher visitValue(ValueModel<T> value) {
		return this.hasher.hash(this.target);
	}

	private <P> Hasher visitProperty(Property<? super T, P> property) {
		return property.accept(new HashModelVisitor<>(property.get(target), hasher));
	}
	
	public <K, V> Hasher visitMap(MapModel<T, K, V> map) {
		map.apply(this.target).entrySet().forEach(e -> map.acceptKey(new HashModelVisitor<>(e.getKey(), map.acceptValue(new HashModelVisitor<>(e.getValue(), hasher)))));
		return this.hasher;
	}

	public <E> Hasher visitSet(SetModel<T, E> values) {
		values.apply(this.target).forEach(e -> values.acceptElement(new HashModelVisitor<>(e, hasher)));
		return this.hasher;
	}
	
	public <E> Hasher visitList(ListModel<T, E> values) {
		values.apply(this.target).forEach(e -> values.acceptElement(new HashModelVisitor<>(e, hasher)));
		return this.hasher;
	}
}