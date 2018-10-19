package org.codegeny.beans.model.visitor;

import static java.util.Comparator.nullsLast;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class CompareModelVisitor<T> implements ModelVisitor<T, Integer> {
	
	private final T left, right;
	
	public CompareModelVisitor(T left, T right) {
		this.left = left;
		this.right = right;
	}
	
	public Integer visitBean(BeanModel<T> bean) {
		return bean.getProperties().stream().mapToInt(this::visitProperty).filter(i -> i != 0).findFirst().orElse(0);
	}
	
	private <P> Integer visitProperty(Property<? super T, P> property) {
		return new ModelComparator<>(property.getModel()).compare(property.get(left), property.get(right));
	}

	public Integer visitValue(ValueModel<T> value) {
		return nullsLast(value).compare(left, right);
	}
	
	public <K, V> Integer visitMap(MapModel<T, K, V> map) {
		Map<? extends K, ? extends V> leftMap = map.apply(this.left);
		Map<? extends K, ? extends V> rightMap = map.apply(this.right);
		Set<K> keys = new TreeSet<>(new ModelComparator<>(map.getKeyModel()));
		keys.addAll(leftMap.keySet());
		keys.addAll(rightMap.keySet());
		Comparator<? super V> valueComparator = new ModelComparator<>(map.getValueModel());
		return keys.stream().mapToInt(k -> valueComparator.compare(leftMap.get(k), rightMap.get(k))).filter(i -> i != 0).findFirst().orElse(0);
	}

	public <E> Integer visitSet(SetModel<T, E> values) {
		Comparator<? super E> comparator = new ModelComparator<>(values.getElementModel());
		Iterator<? extends E> leftIterator = values.apply(this.left).stream().sorted(comparator).iterator();
		Iterator<? extends E> rightIterator = values.apply(this.right).stream().sorted(comparator).iterator();
		while (leftIterator.hasNext() && rightIterator.hasNext()) {
			int comparison = comparator.compare(leftIterator.next(), rightIterator.next());
			if (comparison != 0) {
				return comparison;
			}
		}
		return leftIterator.hasNext() ? -1 : rightIterator.hasNext() ? +1 : 0;
	}
	
	public <E> Integer visitList(ListModel<T, E> values) {
		Comparator<? super E> comparator = new ModelComparator<>(values.getElementModel());
		Iterator<? extends E> leftIterator = values.apply(this.left).stream().iterator();
		Iterator<? extends E> rightIterator = values.apply(this.right).stream().iterator();
		while (leftIterator.hasNext() && rightIterator.hasNext()) {
			int comparison = comparator.compare(leftIterator.next(), rightIterator.next());
			if (comparison != 0) {
				return comparison;
			}
		}
		return leftIterator.hasNext() ? -1 : rightIterator.hasNext() ? +1 : 0;
	}
}
