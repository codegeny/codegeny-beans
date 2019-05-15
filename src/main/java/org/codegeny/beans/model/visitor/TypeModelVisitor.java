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
package org.codegeny.beans.model.visitor;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public final class TypeModelVisitor<T>  implements ModelVisitor<T, Type> {

	private static final class ParameterizedTypeImpl implements ParameterizedType {
		
		private final Type[] actualTypeArguments;
		private final Type rawType;

		private ParameterizedTypeImpl(Type rawType, Type... actualTypeArguments) {
			this.actualTypeArguments = Objects.requireNonNull(actualTypeArguments);
			this.rawType = Objects.requireNonNull(rawType);
		}

		public Type[] getActualTypeArguments() {
			return Arrays.copyOf(actualTypeArguments, actualTypeArguments.length);
		}

		public Type getOwnerType() {
			return null;
		}

		public Type getRawType() {
			return rawType;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(actualTypeArguments) ^ rawType.hashCode();
		}

		@Override
		public boolean equals(Object that) {
			return super.equals(that) || that instanceof ParameterizedType && equals((ParameterizedType) that);
		}
		
		private boolean equals(ParameterizedType that) {
			return that.getOwnerType() == null && rawType.equals(that.getRawType()) && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(rawType.toString());
			if (actualTypeArguments.length > 0) {
				sb.append(Stream.of(actualTypeArguments).map(Object::toString).collect(joining(",", "<", ">")));
			}
			return sb.toString();
		}
	}
	
	@Override
	public Type visitBean(BeanModel<T> bean) {
		return bean.getType();
	}

	@Override
	public <E> Type visitList(ListModel<T, E> list) {
		return new ParameterizedTypeImpl(List.class, list.accept(new TypeModelVisitor<>()));
	}

	@Override
	public <K, V> Type visitMap(MapModel<T, K, V> map) {
		return new ParameterizedTypeImpl(Map.class, map.acceptKey(new TypeModelVisitor<>()), map.acceptValue(new TypeModelVisitor<>()));
	}

	@Override
	public <E> Type visitSet(SetModel<T, E> set) {
		return new ParameterizedTypeImpl(Set.class, set.accept(new TypeModelVisitor<>()));
	}

	@Override
	public Type visitValue(ValueModel<T> value) {
		return value.getType();
	}
}
