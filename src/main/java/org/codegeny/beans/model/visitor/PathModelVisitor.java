package org.codegeny.beans.model.visitor;

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
import org.codegeny.beans.path.IndexPathElement;
import org.codegeny.beans.path.KeyPathElement;
import org.codegeny.beans.path.PathElement;
import org.codegeny.beans.path.PropertyPathElement;

public class PathModelVisitor<T> implements ModelVisitor<T, Object> {
	
	private final Iterator<? extends PathElement> elements;
	private final T target;

	public PathModelVisitor(T target, Iterator<? extends PathElement> elements) {
		this.target = target;
		this.elements = elements;
	}

	private <E extends PathElement, S> Object apply(E element, Model<? super S> model, Function<? super E, ? extends S> extractor) {
		return process(model, extractor.apply(element));
	}
	
	private <E extends PathElement, S> Object process(Class<? extends E> expectedType, Function<? super E, ?> processor) {
		return elements.hasNext() ? processor.apply(expectedType.cast(elements.next())) : target;
	}

	private <E extends PathElement, S> Object process(Class<? extends E> expectedType, Model<? super S> model, Function<? super E, ? extends S> extractor) {
		return process(expectedType, element -> apply(element, model, extractor));
	}
	
	private <S> Object process(Model<? super S> model, S value) {
		return model.accept(new PathModelVisitor<>(value, elements));
	}

	@Override
	public Object visitBean(BeanModel<? super T> bean) {
		return process(PropertyPathElement.class, element -> visitProperty(bean.getProperty(element.getProperty())));
	}

	@Override
	public <E> Object visitList(ListModel<? super T, E> list) {
		return process(IndexPathElement.class, list.getDelegate(), element -> list.apply(target).get(element.getIndex()));
	}

	@Override
	public <K, V> Object visitMap(MapModel<? super T, K, V> map) {
		return process(KeyPathElement.class, map.getValueDelegate(), element -> map.apply(target).get(element.getKey()));
	}
	
	private <P> Object visitProperty(Property<? super T, P> property) {
		return process(property.getDelegate(), property.apply(target));
	}
	
	@Override
	public <E> Object visitSet(SetModel<? super T, E> set) {
		return process(IndexPathElement.class, set.getDelegate(), element -> set.apply(target).stream().skip(element.getIndex()).findFirst().get());
	}
	
	@Override
	public Object visitValue(ValueModel<? super T> value) {
		return process(PathElement.class, element -> {
			throw new IllegalStateException("ValueModels cannot have subpaths");
		});
	}
}
