package org.codegeny.beans.util;

import static java.util.Arrays.asList;
import static java.util.Arrays.deepEquals;
import static java.util.Arrays.deepHashCode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HashKey implements Serializable {

	public static class Builder {

		private final List<Object> elements = new LinkedList<Object>();

		public Builder append(Object[] elements) {
			this.elements.addAll(asList(elements));
			return this;
		}
		
		public Builder append(Object element) {
			this.elements.add(element);
			return this;
		}

		public HashKey build() {
			return new HashKey(this.elements.toArray());
		}
	}
	
	public static HashKey fromArray(Object[] objects) {
		return new HashKey(objects);
	}

	public static final long serialVersionUID = 1L;

	public static Builder builder() {
		return new Builder();
	}

	private final Object[] elements;

	protected HashKey(int size) {
		this.elements = new Object[size];
	}

	public HashKey(Object... elements) {
		this.elements = Object[].class.cast(elements.clone());
	}
	
	public @Override final boolean equals(Object that) {
		return super.equals(that) || that != null && getClass().equals(that.getClass()) && deepEquals(this.elements, ((HashKey) that).elements);
	}

	protected final Object get(int index) {
		return this.elements[index];
	}

	public @Override final int hashCode() {
		return deepHashCode(this.elements);
	}

	protected final void set(int index, Object value) {
		this.elements[index] = value;
	}
	
	public @Override String toString() {
		return Arrays.toString(this.elements);
	}
}
