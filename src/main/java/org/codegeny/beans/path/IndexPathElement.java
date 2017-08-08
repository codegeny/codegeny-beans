package org.codegeny.beans.path;

public final class IndexPathElement implements PathElement {
	
	private static final long serialVersionUID = 1L;

	private final int index;

	IndexPathElement(int index) {
		this.index = index;
	}

	@Override
	public <R> R accept(R parent, PathVisitor<R> visitor) {
		return visitor.visitIndex(parent, index);
	}
	
	public int getIndex() {
		return index;
	}
}