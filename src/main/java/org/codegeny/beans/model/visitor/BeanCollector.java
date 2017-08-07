package org.codegeny.beans.model.visitor;

import java.util.HashSet;
import java.util.Set;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class BeanCollector<T> implements ModelVisitor<T, Set<?>> {
	
	private final Set<Object> beans;
	private final T target;

	public BeanCollector(T target) {
		this(target, new HashSet<>());
	}
	
	private BeanCollector(T target, Set<Object> beans) {
		this.target = target;
		this.beans = beans;
	}

	public Set<?> visitBean(BeanModel<? super T> bean) {
		if (this.target != null) {
			this.beans.add(this.target);
			bean.forEach(this::visitProperty);
		}
		return beans;
	}
	
	public <E> Set<?> visitSet(SetModel<? super T, E> collection) {
		collection.apply(this.target).forEach(e -> collection.acceptDelegate(new BeanCollector<>(e, this.beans)));
		return beans;
	}
	
	public <E> Set<?> visitList(ListModel<? super T, E> collection) {
		collection.apply(this.target).forEach(e -> collection.acceptDelegate(new BeanCollector<>(e, this.beans)));
		return beans;
	}

	public <K, V> Set<?> visitMap(MapModel<? super T, K, V> map) {
		map.apply(this.target).values().forEach(e -> map.acceptValueDelegate(new BeanCollector<>(e, this.beans)));;
		return beans;
	}

	private <P> void visitProperty(Property<? super T, P> property) {
		property.acceptDelegate(new BeanCollector<>(property.apply(this.target), this.beans));
	}

	public Set<?> visitValue(ValueModel<? super T> value) {
		return beans;
	}
}