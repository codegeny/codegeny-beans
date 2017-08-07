package org.codegeny.beans.model.visitor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.codegeny.beans.diff.Diff.Status.ADDED;
import static org.codegeny.beans.diff.Diff.Status.MODIFIED;
import static org.codegeny.beans.diff.Diff.Status.REMOVED;
import static org.codegeny.beans.diff.Diff.Status.UNCHANGED;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.Diff.Status;
import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public abstract class CommonDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {
	
	private static abstract class AbstractDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {
		
		private final T left, right, target;
		private final Status type;

		protected AbstractDiffModelVisitor(T left, T right, T target, Status type) {
			this.left = left;
			this.right = right;
			this.target = target;
			this.type = type;
		}

		protected abstract <N> AbstractDiffModelVisitor<N> create(N value);
		
		public Diff<T> visitBean(BeanModel<? super T> bean) {
			return Diff.bean(type, left, right, bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty)));
		}

		public <E> Diff<T> visitSet(SetModel<? super T, E> values) {
			return Diff.list(type, left, right, values.apply(target).stream().map(e -> values.acceptDelegate(create(e))).collect(toList()));
		}
		
		public <E> Diff<T> visitList(ListModel<? super T, E> values) {
			return Diff.list(type, left, right, values.apply(target).stream().map(e -> values.acceptDelegate(create(e))).collect(toList()));
		}

		public <K, V> Diff<T> visitMap(MapModel<? super T, K, V> map) {
			return Diff.map(type, left, right, map.apply(target).entrySet().stream().collect(toMap(e -> e.getKey(), e -> map.acceptValueDelegate(create(e.getValue())))));
		}
		
		private <P> Diff<P> visitProperty(Property<? super T, P> property) {
			return property.getDelegate().accept(create(property.apply(target)));
		}
		
		public Diff<T> visitValue(ValueModel<? super T> value) {
			return Diff.simple(type, left, right);
		}
	}
		
	protected static class AddedDiffModelVisitor<T> extends AbstractDiffModelVisitor<T> {
		
		public AddedDiffModelVisitor(T right) {
			super(null, right, right, ADDED);
		}
		
		protected <N> AbstractDiffModelVisitor<N> create(N value) {
			return new AddedDiffModelVisitor<>(value);
		}
	}
	
	protected static class NullDiffModelVisitor<T> implements ModelVisitor<T, Diff<T>> {

		public Diff<T> visitBean(BeanModel<? super T> bean) {
			return Diff.bean(UNCHANGED, null, null, bean.getProperties().stream().collect(toMap(Property::getName, p -> p.getDelegate().accept(new NullDiffModelVisitor<>()))));
		}
		
		public <E> Diff<T> visitSet(SetModel<? super T, E> collection) {
			return Diff.list(UNCHANGED, null, null, emptyList());
		}
		
		public <E> Diff<T> visitList(ListModel<? super T, E> collection) {
			return Diff.list(UNCHANGED, null, null, emptyList());
		}

		public <K, V> Diff<T> visitMap(MapModel<? super T, K, V> map) {
			return Diff.map(UNCHANGED, null, null, emptyMap());
		}
		
		public Diff<T> visitValue(ValueModel<? super T> value) {
			return Diff.simple(UNCHANGED, null, null);
		}
	}
	
	protected static class RemovedDiffModelVisitor<T> extends AbstractDiffModelVisitor<T> {
		
		public RemovedDiffModelVisitor(T left) {
			super(left, null, left, REMOVED);
		}
		
		protected <N> AbstractDiffModelVisitor<N> create(N value) {
			return new RemovedDiffModelVisitor<>(value);
		}
	}

	private static <X> X throwingBinaryOperator(X left, X right) {
		throw new IllegalStateException(String.format("Duplicate key %s", left));
	}
	
	protected static Status toStatus(Collection<? extends Diff<?>> diffs) {
		return diffs.stream().map(Diff::getStatus).reduce(Status::combine).orElse(UNCHANGED);
	}
	
	protected final T left, right;
	
	public CommonDiffModelVisitor(T left, T right) {
		this.left = left;
		this.right = right;
	}
	
	protected abstract <S> ModelVisitor<S, Diff<S>> newVisitor(S left, S right);
	
	public Diff<T> visitBean(BeanModel<? super T> bean) {
		if (left == null ^ right == null) {
			return bean.accept(left == null ? new AddedDiffModelVisitor<>(right) : new RemovedDiffModelVisitor<>(left));
		}
		Map<String, Diff<?>> properties = bean.getProperties().stream().collect(toMap(Property::getName, this::visitProperty, CommonDiffModelVisitor::throwingBinaryOperator, LinkedHashMap::new));
		return Diff.bean(toStatus(properties.values()), this.left, this.right, properties);
	}
	
	@Override
	public <K, V> Diff<T> visitMap(MapModel<? super T, K, V> map) {
		if (left == null ^ right == null) {
			return map.accept(left == null ? new AddedDiffModelVisitor<>(right) : new RemovedDiffModelVisitor<>(left));
		}
		Map<? extends K, ? extends V> leftMap = map.apply(left);
		Map<? extends K, ? extends V> rightMap = map.apply(right);
		Set<K> keys = new HashSet<>();
		keys.addAll(leftMap.keySet());
		keys.addAll(rightMap.keySet());
		Map<K, Diff<V>> result = keys.stream().collect(toMap(k -> k, k -> map.acceptValueDelegate(newVisitor(leftMap.get(k), rightMap.get(k)))));
		return Diff.map(toStatus(result.values()), this.left, this.right, result);
	}
		
	private <P> Diff<P> visitProperty(Property<? super T, P> property) {
		return property.getDelegate().accept(newVisitor(property.apply(left), property.apply(right)));
	}
	
	public Diff<T> visitValue(ValueModel<? super T> value) {
		return Diff.simple(left == null ^ right == null ? left == null ? ADDED : REMOVED : Objects.equals(left, right) ? UNCHANGED : MODIFIED, left, right); 
	}
}