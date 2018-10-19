package org.codegeny.beans.model.visitor;

import static java.util.function.Predicate.isEqual;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.Model;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;
import org.codegeny.beans.path.Path;

public final class SetModelVisitor<S, T> implements ModelVisitor<T, Void> {
	
	private final T current;
	private final S valueToSet;
	private final Iterator<? extends S> path;
	private final Consumer<? super T> setter;
	private final Typer<S> typer;
	
	public SetModelVisitor(T current, S valueToSet, Typer<S> typer, Path<S> path) {
		this(current, valueToSet, path.iterator(), typer, a -> {
			throw new UnsupportedOperationException("Cannot set root object");
		});
	}
	
	private SetModelVisitor(T current, S valueToSet, Iterator<? extends S> path, Typer<S> typer, Consumer<? super T> setter) {
		this.current = current;
		this.valueToSet = valueToSet;
		this.path = path;
		this.typer = typer;
		this.setter = setter;
	}

	@Override
	public Void visitBean(BeanModel<T> bean) {
		return process(k -> visitProperty(bean.getProperty(typer.retype(Model.STRING, k))), setter, bean);
	}
	
	private <P> void visitProperty(Property<T, P> property) {
		property.accept(visitor(property.get(current), a -> property.set(current, a)));
	}
	
	@Override
	public <K, V> Void visitMap(MapModel<T, K, V> map) {
		Map<K, V> m = map.apply(current);
		return process(map, map.getValueModel(), map.getKeyModel(), m::get, m::put, t -> {
			m.clear();
			m.putAll(map.apply(t));
		});
	}
	
	@Override
	public <E> Void visitSet(SetModel<T, E> set) {
		Set<E> s = set.apply(current);
		return process(set, set.getElementModel(), set.getElementModel(), v -> s.stream().filter(isEqual(v)).findAny().orElse(null), (a, b) -> s.add(a), t -> {
			s.clear();
			s.addAll(set.apply(t));
		});
	}
	
	@Override
	public <E> Void visitList(ListModel<T, E> list) {
		List<E> l = list.apply(current);
		return process(list, list.getElementModel(), Model.INTEGER, l::get, l::set, t -> {
			l.clear();
			l.addAll(list.apply(t));
		});
	}
	
	@Override
	public Void visitValue(ValueModel<T> value) {
		return process(p -> {
			throw new UnsupportedOperationException("Value object must be terminal");
		}, setter, value);
	}
	
	private Void process(Consumer<? super S> p, Consumer<? super T> c, Model<? extends T> t) {
		if (path.hasNext()) {
			p.accept(path.next());
		} else {
			c.accept(typer.retype(t, valueToSet));
		}
		return null;
	}
	
	private <I, E> Void process(Model<? extends T> t, Model<E> e, Model<? extends I> i, Function<? super I, ? extends E> f, BiConsumer<? super I, ? super E> b, Consumer<? super T> s) {
		return process(k -> {
			I value = typer.retype(i, k);
			e.accept(visitor(f.apply(value), z -> b.accept(value, z)));
		}, s, t);
	}
	
	private <Z> SetModelVisitor<S, Z> visitor(Z current, Consumer<? super Z> setter) {
		return new SetModelVisitor<>(current, valueToSet, path, typer, setter);
	}
}
