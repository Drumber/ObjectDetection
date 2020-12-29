package de.lars.mrod.core.utils;

import java.util.concurrent.TimeUnit;

public class Stopwatch {
	
	private TimeUnit timeUnit;
	private long lastTime;
	private long lastDuration;
	
	public Stopwatch() {
		this(TimeUnit.MILLISECONDS);
	}
	
	public Stopwatch(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
		reset();
	}
	
	/**
	 * Stop the timer and get the duration
	 * @return	duration
	 */
	public long stop() {
		lastDuration = System.nanoTime() - lastTime;
		return getLastDuration();
	}
	
	public long getLastDuration() {
		return conv(lastDuration);
	}
	
	public long getCurrentDuration() {
		return conv(System.nanoTime() - lastTime);
	}
	
	public void reset() {
		lastTime = System.nanoTime();
		lastDuration = 0;
	}
	
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	private long conv(long val) {
		return timeUnit.convert(val, TimeUnit.NANOSECONDS);
	}

}
