package org.codegeny.beans.model.visitor.json;

/*-
 * #%L
 * codegeny-beans
 * %%
 * Copyright (C) 2016 - 2018 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
