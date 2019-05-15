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
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;
import org.codegeny.beans.util.Hasher;

public class HashModelVisitor<T> implements ModelVisitor<T, Hasher> {
	
	private final Hasher hasher;
	private final T target;

	public HashModelVisitor(T target, Hasher hasher) {
		this.target = target;
		this.hasher = hasher;
	}

	public Hasher visitBean(BeanModel<T> bean) {
		bean.getProperties().forEach(this::visitProperty);
		return hasher;
	}

	public Hasher visitValue(ValueModel<T> value) {
		return this.hasher.hash(this.target);
	}

	private <P> Hasher visitProperty(Property<? super T, P> property) {
		return property.accept(new HashModelVisitor<>(property.get(target), hasher));
	}
	
	public <K, V> Hasher visitMap(MapModel<T, K, V> map) {
		map.toMap(this.target).forEach((k, v) -> map.acceptKey(new HashModelVisitor<>(k, map.acceptValue(new HashModelVisitor<>(v, hasher)))));
		return this.hasher;
	}

	public <E> Hasher visitSet(SetModel<T, E> values) {
		values.toSet(this.target).forEach(e -> values.acceptElement(new HashModelVisitor<>(e, hasher)));
		return this.hasher;
	}
	
	public <E> Hasher visitList(ListModel<T, E> values) {
		values.toList(this.target).forEach(e -> values.acceptElement(new HashModelVisitor<>(e, hasher)));
		return this.hasher;
	}
}
