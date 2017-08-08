package org.codegeny.beans.path;

public final class KeyPathElement implements PathElement {

	private static final long serialVersionUID = 1L;
	
	private final Object key;

	KeyPathElement(Object key) {
		this.key = key;
	}

	@Override
	public <R> R accept(R parent, PathVisitor<R> visitor) {
		return visitor.visitKey(parent, key);
	}
	
	public Object getKey() {
		return key;
	}
}