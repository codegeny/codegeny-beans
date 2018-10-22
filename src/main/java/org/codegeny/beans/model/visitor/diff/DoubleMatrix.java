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

public class DoubleMatrix implements Matrix<Double> {
	
	private final int rows, columns;
	private final double[][] values;
	
	public DoubleMatrix(int rows, int columns) {
		this.values = new double[this.rows = rows][this.columns = columns];
	}

	public Double get(int row, int column) {
		return values[row][column];
	}

	public int getColumns() {
		return columns;
	}
	
	public int getRows() {
		return rows;
	}
	
	public void set(int row, int column, Double value) {
		values[row][column] = value;
	}
}
