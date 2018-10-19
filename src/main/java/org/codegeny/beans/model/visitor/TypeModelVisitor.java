package org.codegeny.beans.model.visitor;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class TypeModelVisitor<T> implements ModelVisitor<T, Class<? extends T>> {

	@Override
	public Class<? extends T> visitBean(BeanModel<T> bean) {
		return bean.getType();
	}

	@Override
	public <E> Class<? extends T> visitList(ListModel<T, E> list) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <K, V> Class<? extends T> visitMap(MapModel<T, K, V> map) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E> Class<? extends T> visitSet(SetModel<T, E> set) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends T> visitValue(ValueModel<T> value) {
		return value.getType();
	}
}
