package org.codegeny.beans.model.visitor.diff;

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
