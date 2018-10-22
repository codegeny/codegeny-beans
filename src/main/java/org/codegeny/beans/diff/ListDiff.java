package org.codegeny.beans.diff;

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
import static java.util.Collections.unmodifiableList;

import java.util.List;

/**
 * Implementation of <code>{@link Diff}</code> for lists.
 *
 * @author Xavier DURY
 * @param <L> The type of list.
 * @param <E> The type of list element.
 */
public final class ListDiff<L, E> extends AbstractDiff<L> {
	
	private static final long serialVersionUID = 1L;
	
	private final List<Diff<E>> list;

	ListDiff(Status status, L left, L right, List<? extends Diff<E>> list) {
		super(list, status, left, right);
		this.list = unmodifiableList(list);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(DiffVisitor<L, R> visitor) {
		return visitor.visitList(this);
	}
	
	/**
	 * Get the list of diff'ed elements.
	 * 
	 * @return The list.
	 */
	public List<Diff<E>> getList() {
		return list;
	}
}
