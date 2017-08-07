package org.codegeny.beans.model.visitor;

import java.util.Comparator;

import org.codegeny.beans.model.Model;

public class ModelComparator<T> implements Comparator<T> {
	
	private final Model<T> model;

	public ModelComparator(Model<T> model) {
		this.model = model;
	}
	
	@Override
	public int compare(T left, T right) {
		return this.model.accept(new CompareModelVisitor<>(left, right));
	}
}
