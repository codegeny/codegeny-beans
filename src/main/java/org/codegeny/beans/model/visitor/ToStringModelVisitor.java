package org.codegeny.beans.model.visitor;

import static java.util.stream.Collectors.toList;
import static org.codegeny.beans.util.IndexedConsumer.forEachIndexed;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class ToStringModelVisitor<T> implements ModelVisitor<T, StringBuilder> {
	
	private final StringBuilder builder;
	private final String indent;
	private final T target;
	
	public ToStringModelVisitor(T target) {
		this(target, new StringBuilder(), "");
	}
	
	private ToStringModelVisitor(T target, StringBuilder builder, String indent) {
		this.target = target;
		this.builder = builder;
		this.indent = indent;
	}
	
	public StringBuilder visitBean(BeanModel<T> bean) {
		this.builder.append("{");
		int count = forEachIndexed(bean.getProperties(), (i, p) -> {
			this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ").append(p.getName()).append(": ");
			visitProperty(p);
		});
		return this.builder.append(count > 0 ? "\n" : "").append(this.indent).append("}"); 
	}
	
	private <P> StringBuilder visitProperty(Property<? super T, P> property) {
		return property.accept(new ToStringModelVisitor<>(property.get(this.target), this.builder, this.indent.concat("  ")));
	}
	
	public StringBuilder visitValue(ValueModel<T> value) {
		return this.builder.append(this.target);
	}
	
	public <K, V> StringBuilder visitMap(MapModel<T, K, V> map) {
		this.builder.append("[");
		Comparator<? super K> comparator = new ModelComparator<>(map.getKeyModel());
		Map<? extends K, ? extends V> entries = map.apply(this.target);
		Collection<K> sorted = entries.keySet().stream().sorted(comparator).collect(toList());
		int count = forEachIndexed(sorted, (i, v) -> {
			this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ").append(v).append(": ");
			map.acceptValue(new ToStringModelVisitor<>(entries.get(v), this.builder, this.indent.concat("  ")));
		});
		return this.builder.append(count > 0 ? "\n".concat(this.indent) : "").append("]");
	}

	public <E> StringBuilder visitSet(SetModel<T, E> values) {
		this.builder.append("[");
		Comparator<? super E> comparator = new ModelComparator<>(values.getElementModel());
		Set<? extends E> collection = values.apply(this.target);
		Collection<E> sorted = collection.stream().sorted(comparator).collect(toList());
		int count = forEachIndexed(sorted, (i, v) -> {
			this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ");
			values.acceptElement(new ToStringModelVisitor<>(v, this.builder, this.indent.concat("  ")));
		});
		return this.builder.append(count > 0 ? "\n".concat(this.indent) : "").append("]");
	}
	
	public <E> StringBuilder visitList(ListModel<T, E> values) {
		this.builder.append("[");
		List<? extends E> list = values.apply(this.target);
		int count = forEachIndexed(list, (i, v) -> {
			this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ");
			values.acceptElement(new ToStringModelVisitor<>(v, this.builder, this.indent.concat("  ")));
		});
		return this.builder.append(count > 0 ? "\n".concat(this.indent) : "").append("]");
	}
}