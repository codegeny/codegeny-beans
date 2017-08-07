package org.codegeny.beans.path.visitor;

import org.codegeny.beans.path.PathVisitor;

public class ToStringPathVisitor implements PathVisitor<StringBuilder> {
	
	@Override
	public StringBuilder visitIndex(StringBuilder parent, int index) {
		return parent.append("[").append(index).append("]");
	}

	@Override
	public StringBuilder visitKey(StringBuilder parent, Object key) {
		return parent.append("(").append(key).append(")");
	}

	@Override
	public StringBuilder visitProperty(StringBuilder parent, String property) {
		return parent.append(".").append(property);
	}
}