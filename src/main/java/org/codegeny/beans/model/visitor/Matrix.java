package org.codegeny.beans.model.visitor;

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
