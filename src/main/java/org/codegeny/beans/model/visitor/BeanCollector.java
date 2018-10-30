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

import java.util.HashSet;
import java.util.Set;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class BeanCollector<T> implements ModelVisitor<T, Set<?>> {
	
	private final Set<Object> beans;
	private final T target;

	public BeanCollector(T target) {
		this(target, new HashSet<>());
	}
	
	private BeanCollector(T target, Set<Object> beans) {
		this.target = target;
		this.beans = beans;
	}

	public Set<?> visitBean(BeanModel<T> bean) {
		if (this.target != null) {
			this.beans.add(this.target);
			bean.getProperties().forEach(this::visitProperty);
		}
		return beans;
	}
	
	public <E> Set<?> visitSet(SetModel<T, E> collection) {
		collection.toSet(this.target).forEach(e -> collection.acceptElement(new BeanCollector<>(e, this.beans)));
		return beans;
	}
	
	public <E> Set<?> visitList(ListModel<T, E> collection) {
		collection.toList(this.target).forEach(e -> collection.acceptElement(new BeanCollector<>(e, this.beans)));
		return beans;
	}

	public <K, V> Set<?> visitMap(MapModel<T, K, V> map) {
		map.toMap(this.target).values().forEach(e -> map.acceptValue(new BeanCollector<>(e, this.beans)));
		return beans;
	}

	private <P> void visitProperty(Property<? super T, P> property) {
		property.accept(new BeanCollector<>(property.get(this.target), this.beans));
	}

	public Set<?> visitValue(ValueModel<T> value) {
		return beans;
	}
}
