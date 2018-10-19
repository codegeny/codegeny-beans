package org.codegeny.beans.util;

/**
 * This implementation of hasher is not affected by the order of elements which are hashed.
 * 
 * @author Xavier DURY
 */
public class AddAndXorHasher implements Hasher {
	
	private static final int MASK = 0xaaaaaaaa;

	private int add, xor;
	
	public Hasher hash(int value) {
		add += value;
		xor ^= value;
		return this;
	}
	
	public int toHash() {
		return (MASK & add) | (~MASK & xor); 
	}
}