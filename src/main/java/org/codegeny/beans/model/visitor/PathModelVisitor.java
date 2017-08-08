package org.codegeny.beans.model.visitor;

import java.util.Iterator;
import java.util.function.Function;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.Model;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;
import org.codegeny.beans.path.IndexPathElement;
import org.codegeny.beans.path.KeyPathElement;
import org.codegeny.beans.path.PathElement;
import org.codegeny.beans.path.PropertyPathElement;

// TOOD revisit private methods here
public class PathModelVisitor<T> implements ModelVisitor<T, Object> {
	
	private final T target;
	private final Iterator<PathElement> elements;

	public PathModelVisitor(T target, Iterator<PathElement> elements) {
		this.target = target;
		this.elements = elements;
	}

	@Override
	public Object visitBean(BeanModel<? super T> bean) {
		return process(
			element -> bean.getProperty(((PropertyPathElement) element).getProperty()).getDelegate(),
			element -> bean.getProperty(((PropertyPathElement) element).getProperty()).apply(target)
		);
	}

	@Override
	public <E> Object visitList(ListModel<? super T, E> list) {
		return process(
			element -> list.getDelegate(),
			element -> list.apply(target).get(((IndexPathElement) element).getIndex())
		);
	}

	@Override
	public <K, V> Object visitMap(MapModel<? super T, K, V> map) {
		return process(
			element -> map.getValueDelegate(),
			element -> map.apply(target).get(((KeyPathElement) element).getKey())
		);
	}

	@Override
	public <E> Object visitSet(SetModel<? super T, E> set) {
		return process(
			element -> set.getDelegate(),
			element -> set.apply(target).stream().skip(((IndexPathElement) element).getIndex()).findFirst().get()
		);
	}

	@Override
	public Object visitValue(ValueModel<? super T> value) {
		return target;
	}
	
	private <S, M extends Model<S>> Object process(Function<PathElement, M> processor, Function<PathElement, S> extractor) {
		return elements.hasNext() ? apply(elements.next(), processor, extractor) : target;
	}
	
	private <S, M extends Model<S>> Object apply(PathElement element, Function<PathElement, M> processor, Function<PathElement, S> extractor) {
		return processor.apply(element).accept(new PathModelVisitor<>(extractor.apply(element), elements));
	}
}
