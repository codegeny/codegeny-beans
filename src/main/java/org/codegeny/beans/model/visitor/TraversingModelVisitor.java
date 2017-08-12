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
	
	private final Path path;
	private final BiConsumer<? super Path, Object> processor;
	private final T target;

	public TraversingModelVisitor(T target, BiConsumer<? super Path, Object> processor) {
		this(target, Path.path(), processor);
	}

	private TraversingModelVisitor(T target, Path path, BiConsumer<? super Path, Object> processor) {
		this.target = target;
		this.path = path;
		this.processor = processor;
	}
	
	private <R> TraversingModelVisitor<R> childVisitor(R target, Path path) {
		return new TraversingModelVisitor<>(target, path, processor);
	}

	private void process() {
		this.processor.accept(this.path, this.target);
	}
	
	@Override
	public Void visitBean(BeanModel<? super T> bean) {
		process();
		bean.getProperties().forEach(this::visitProperty);
		return null;
	}
	
	@Override
	public <E> Void visitList(ListModel<? super T, E> list) {
		process();
		forEachIndexed(list.apply(this.target), (i, n) -> list.acceptDelegate(childVisitor(n, path.index(i)))); 
		return null;
	}

	@Override
	public <K, V> Void visitMap(MapModel<? super T, K, V> map) {
		process();
		map.apply(this.target).forEach((k, v) -> map.acceptValueDelegate(childVisitor(v, path.key(k))));
		return null;
	}

	private <P> void visitProperty(Property<? super T, P> property) {
		property.acceptDelegate(childVisitor(property.apply(this.target), this.path.property(property.getName())));
	}

	@Override
	public <E> Void visitSet(SetModel<? super T, E> set) {
		process();
		forEachIndexed(set.apply(this.target), (i, n) -> set.acceptDelegate(childVisitor(n, path.index(i)))); 
		return null;
	}

	@Override
	public Void visitValue(ValueModel<? super T> value) {
		process();
		return null;
	}
}
