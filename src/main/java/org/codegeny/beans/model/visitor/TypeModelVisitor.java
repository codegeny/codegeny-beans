package org.codegeny.beans.model.visitor;

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
