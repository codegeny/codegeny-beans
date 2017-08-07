package org.codegeny.beans.util;

import java.util.Objects;

public interface Hasher {
	
	default Hasher hash(boolean value) {
		return hash(Boolean.hashCode(value));
	}
	
	default Hasher hash(byte value) {
		return hash(Byte.hashCode(value));
	}
	
	default Hasher hash(char value) {
		return hash(Character.hashCode(value));
	}
	
	default Hasher hash(double value) {
		return hash(Double.hashCode(value));
	}
	
	default Hasher hash(float value) {
		return hash(Float.hashCode(value));
	}
	
	Hasher hash(int value);
	
	default Hasher hash(long value) {
		return hash(Long.hashCode(value));
	}
	
	default Hasher hash(Object value) {
		return hash(Objects.hashCode(value));
	}
	
	default Hasher hash(short value) {
		return hash(Short.hashCode(value));
	}
	
	int toHash();
}