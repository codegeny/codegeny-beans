package org.codegeny.beans.model.visitor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.Model;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public final class PathToModelVisitor<S, T> implements ModelVisitor<T, Model<?>> {
	
	private final Iterator<? extends S> path;
	private final Typer<S> typer;
	
	@SafeVarargs
	public PathToModelVisitor(Typer<S> typer, S... path) {
		this(Arrays.asList(path).iterator(), typer);
	}
	
	private PathToModelVisitor(Iterator<? extends S> path, Typer<S> typer) {
		this.path = path;
		this.typer = typer;
	}

	@Override
	public Model<?> visitBean(BeanModel<T> bean) {
		return process(bean, k -> visitProperty(bean.getProperty(typer.retype(Model.STRING, k))));
	}
	
	private <P> Model<?> visitProperty(Property<T, P> property) {
		return property.accept(visitor());
	}
	
	@Override
	public <K, V> Model<?> visitMap(MapModel<T, K, V> map) {
		return process2(map, map.getValueModel());
	}
	
	@Override
	public <E> Model<?> visitSet(SetModel<T, E> set) {
		return process2(set, set.getElementModel());
	}
	
	@Override
	public <E> Model<?> visitList(ListModel<T, E> list) {
		return process2(list, list.getElementModel());
	}
	
	@Override
	public Model<?> visitValue(ValueModel<T> value) {
		return process(value, p -> {
			throw new UnsupportedOperationException("Value object must be terminal");
		});
	}
	
	private Model<?> process(Model<?> model, Function<? super S, Model<?>> p) {
		return path.hasNext() ? p.apply(path.next()) : model;
	}
	
	private <I, E> Model<?> process2(Model<T> t, Model<E> e) {
		return process(t, k -> e.accept(visitor()));
	}
	
	private <Z> PathToModelVisitor<S, Z> visitor() {
		return new PathToModelVisitor<>(path, typer);
	}
}
