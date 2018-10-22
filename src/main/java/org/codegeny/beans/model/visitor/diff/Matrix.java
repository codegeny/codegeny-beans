package org.codegeny.beans.model.visitor.diff;

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

public interface Matrix<T> {
	
	int getRows();
	
	int getColumns();
	
	T get(int row, int column);
	
	void set(int row, int column, T value);
	
	default Matrix<T> flip() {
		return new Matrix<T>() {

			public int getRows() {
				return Matrix.this.getColumns();
			}

			public int getColumns() {
				return Matrix.this.getRows();
			}

			public T get(int row, int column) {
				return Matrix.this.get(column, row);
			}
			
			public void set(int row, int column, T value) {
				Matrix.this.set(column, row, value);
			}
		};
	}
}
