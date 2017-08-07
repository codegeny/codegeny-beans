package org.codegeny.beans.model.visitor;

import static java.util.stream.Collectors.toList;
import static org.codegeny.beans.util.IndexedConsumer.forEachIndexed;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class ToPathModelVisitor<T> implements ModelVisitor<T, Map<String, ?>> {

	private final Map<String, Object> map;
	private final String path;
	private final T target;
	
	public ToPathModelVisitor(T target, String root) {
		this(target, root, new LinkedHashMap<String, Object>());
	}
	
	private ToPathModelVisitor(T target, String path, Map<String, Object> map) {
		this.target = target;
		this.path = path;
		this.map = map;
	}

	public Map<String, ?> visitBean(BeanModel<? super T> bean) {
		bean.getProperties().forEach(this::visitProperty);
		return map;
	}
	
	private <P> Map<String, ?> visitProperty(Property<? super T, P> property) {
		return property.acceptDelegate(new ToPathModelVisitor<>(property.apply(this.target), this.path + "." + property.getName(), map));
	}
	
	public Map<String, ?> visitValue(ValueModel<? super T> value) {
		map.put(this.path, this.target);
		return map;
	}
	
	public <K, V> Map<String, ?> visitMap(MapModel<? super T, K, V> map) {
		forEachIndexed(map.apply(this.target).entrySet().stream().collect(toList()), (i, e) -> map.acceptValueDelegate(new ToPathModelVisitor<>(e.getValue(), this.path + "[" + e.getKey() + "]", this.map)));
		return this.map;
	}

	public <E> Map<String, ?> visitSet(SetModel<? super T, E> values) {
		forEachIndexed(values.apply(this.target), (i, e) -> values.acceptDelegate(new ToPathModelVisitor<>(e, this.path + "[" + i + "]", map)));
		return map;
	}
	
	public <E> Map<String, ?> visitList(ListModel<? super T, E> values) {
		forEachIndexed(values.apply(this.target), (i, e) -> values.acceptDelegate(new ToPathModelVisitor<>(e, this.path + "[" + i + "]", map)));
		return map;
	}
}