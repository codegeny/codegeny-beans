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

import java.util.Comparator;

import org.codegeny.beans.model.Model;

public final class ModelComparator<T> implements Comparator<T> {
	
	private final Model<T> model;

	public ModelComparator(Model<T> model) {
		this.model = model;
	}
	
	@Override
	public int compare(T left, T right) {
		return this.model.accept(new CompareModelVisitor<>(left, right));
	}
}
