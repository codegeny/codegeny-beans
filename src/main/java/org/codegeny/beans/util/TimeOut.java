package org.codegeny.beans.util;

import java.util.concurrent.TimeUnit;

public class TimeOut {
	
	private static final long COUNTER = 1000000;
	
	private final long limit;
	private long counter = COUNTER;
	
	public TimeOut(long duration, TimeUnit unit) {
		this.limit = System.currentTimeMillis() + unit.toMillis(duration);
	}
	
	public void check() {
		if (--counter == 0) {
			counter = COUNTER;
			if (this.limit < System.currentTimeMillis()) {
				throw new RuntimeException("TIMEOUT!");
			}
		}
	}
}
