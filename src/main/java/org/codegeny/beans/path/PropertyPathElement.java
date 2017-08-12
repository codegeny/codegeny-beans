package org.codegeny.beans.path;

public final class PropertyPathElement implements PathElement {

	private static final long serialVersionUID = 1L;
	
	private final String property;

	PropertyPathElement(String property) {
		this.property = property;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(R parent, PathVisitor<R> visitor) {
		return visitor.visitProperty(parent, property);
	}
	
	public String getProperty() {
		return property;
	}
}