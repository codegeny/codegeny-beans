package org.codegeny.beans.model.visitor;

import static org.codegeny.beans.util.IndexedConsumer.forEachIndexed;

import java.util.function.BiConsumer;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;
import org.codegeny.beans.path.Path;

/**
 * TODO
 * 
 * @author Xavier DURY
 * @param <T> TODO
 */
public final class TraversingModelVisitor<T> implements ModelVisitor<T, Void> {
	
	private final Path<Object> path;
	private final BiConsumer<? super Path<Object> , Object> processor;
	private final T target;

	public TraversingModelVisitor(T target, BiConsumer<? super Path<Object>, Object> processor) {
		this(target, Path.root(), processor);
	}

	private TraversingModelVisitor(T target, Path<Object> path, BiConsumer<? super Path<Object>, Object> processor) {
		this.target = target;
		this.path = path;
		this.processor = processor;
	}
	
	private <R> TraversingModelVisitor<R> childVisitor(R target, Path<Object> path) {
		return new TraversingModelVisitor<>(target, path, processor);
	}

	private void process() {
		this.processor.accept(this.path, this.target);
	}
	
	@Override
	public Void visitBean(BeanModel<T> bean) {
		process();
		bean.getProperties().forEach(this::visitProperty);
		return null;
	}
	
	@Override
	public <E> Void visitList(ListModel<T, E> list) {
		process();
		forEachIndexed(list.apply(this.target), (i, n) -> list.acceptElement(childVisitor(n, path.append(i)))); 
		return null;
	}

	@Override
	public <K, V> Void visitMap(MapModel<T, K, V> map) {
		process();
		map.apply(this.target).forEach((k, v) -> map.acceptValue(childVisitor(v, path.append(k))));
		return null;
	}

	private <P> void visitProperty(Property<T, P> property) {
		property.accept(childVisitor(property.get(this.target), this.path.append(property.getName())));
	}

	@Override
	public <E> Void visitSet(SetModel<T, E> set) {
		process();
		forEachIndexed(set.apply(this.target), (i, n) -> set.acceptElement(childVisitor(n, path.append(i)))); 
		return null;
	}

	@Override
	public Void visitValue(ValueModel<T> value) {
		process();
		return null;
	}
}