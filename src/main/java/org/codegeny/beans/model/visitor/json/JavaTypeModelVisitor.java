package org.codegeny.beans.model.visitor.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JavaTypeModelVisitor<T> implements ModelVisitor<T, JavaType> {
	
	private final TypeFactory typeFactory;

	public JavaTypeModelVisitor(TypeFactory typeFactory) {
		this.typeFactory = typeFactory;
	}

	@Override
	public JavaType visitBean(BeanModel<T> bean) {
		return typeFactory.constructType(bean.getType());
	}

	@Override
	public <E> JavaType visitList(ListModel<T, E> list) {
		return typeFactory.constructCollectionLikeType(List.class, list.acceptElement(newVisitor()));
	}

	@Override
	public <K, V> JavaType visitMap(MapModel<T, K, V> map) {
		return typeFactory.constructMapLikeType(Map.class, map.acceptKey(newVisitor()), map.acceptValue(newVisitor()));
	}

	@Override
	public <E> JavaType visitSet(SetModel<T, E> set) {
		return typeFactory.constructCollectionLikeType(Set.class, set.acceptElement(newVisitor()));
	}

	@Override
	public JavaType visitValue(ValueModel<T> value) {
		return typeFactory.constructType(value.getType());
	}
	
	private <S> JavaTypeModelVisitor<S> newVisitor() {
		return new JavaTypeModelVisitor<>(typeFactory);
	}
}
