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

	public Hasher visitBean(BeanModel<? super T> bean) {
		bean.forEach(this::visitProperty);
		return hasher;
	}

	public Hasher visitValue(ValueModel<? super T> value) {
		return this.hasher.hash(this.target);
	}

	private <P> Hasher visitProperty(Property<? super T, P> property) {
		return property.acceptDelegate(new HashModelVisitor<>(property.apply(target), hasher));
	}
	
	public <K, V> Hasher visitMap(MapModel<? super T, K, V> map) {
		map.apply(this.target).entrySet().forEach(e -> map.acceptKeyDelegate(new HashModelVisitor<>(e.getKey(), map.acceptValueDelegate(new HashModelVisitor<>(e.getValue(), hasher)))));
		return this.hasher;
	}

	public <E> Hasher visitSet(SetModel<? super T, E> values) {
		values.apply(this.target).forEach(e -> values.acceptDelegate(new HashModelVisitor<>(e, hasher)));
		return this.hasher;
	}
	
	public <E> Hasher visitList(ListModel<? super T, E> values) {
		values.apply(this.target).forEach(e -> values.acceptDelegate(new HashModelVisitor<>(e, hasher)));
		return this.hasher;
	}
}