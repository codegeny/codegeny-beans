package org.codegeny.beans.util;

/**
 * This implementation of hasher is not affected by the order of elements which are hashed.
 * 
 * @author Xavier DURY
 */
public class AddAndXorHasher implements Hasher {
	
	private static final int ADD_MASK = 0b10101010101010101010101010101010;
	private static final int XOR_MASK = 0b01010101010101010101010101010101;

	private int xor = 0, add = 0;
	
	public Hasher hash(int value) {
		xor ^= value;
		add += value;
		return this;
	}
	
	public int toHash() {
		return (xor & XOR_MASK) | (add & ADD_MASK); 
	}
}