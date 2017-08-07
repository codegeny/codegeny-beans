package org.codegeny.beans.path;

public interface PathVisitor<R> {
	
	R visitIndex(R parent, int index);
	
	R visitKey(R parent, Object key);
	
	R visitProperty(R parent, String property);
}