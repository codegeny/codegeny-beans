package org.codegeny.beans.diff.visitor;

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

import java.util.Iterator;
import java.util.function.Function;

import org.codegeny.beans.diff.BeanDiff;
import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.DiffVisitor;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.path.Path;

public final class GetDiffVisitor<T> implements DiffVisitor<T, Diff<?>> {
	
	private final Iterator<?> path;

	public GetDiffVisitor(Path<?> path) {
		this(path.iterator());
	}
	
	private GetDiffVisitor(Iterator<?> path) {
		this.path = path;
	}

	@Override
	public Diff<?> visitBean(BeanDiff<T> bean) {
		return process(bean, n -> bean.getProperty((String) n));
	}
	
	@Override
	public <E> Diff<?> visitList(ListDiff<T, E> list) {
		return process(list, n -> list.getList().get((Integer) n));
	}

	@Override
	public <K, V> Diff<?> visitMap(MapDiff<T, K, V> map) {
		return process(map, n -> map.getMap().get(n));
	}

	@Override
	public Diff<?> visitSimple(SimpleDiff<T> simple) {
		return process(simple, n -> {
			throw new IllegalArgumentException("SimpleDiff must be terminal");
		});
	}
	
	private <N> Diff<?> process(Diff<T> diff, Function<Object, Diff<N>> next) {
		return path.hasNext() ? next.apply(path.next()).accept(new GetDiffVisitor<>(path)) : diff;
	}
}
