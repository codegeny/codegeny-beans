package org.codegeny.beans.diff;

import static java.util.Objects.requireNonNull;

import java.util.Collection;

/**
 * Abstract implementation of <code>{@link Diff}</code>.
 *
 * @author Xavier DURY
 * @param <T> The type of the 2 compared objects.
 */
public abstract class AbstractDiff<T> implements Diff<T> {
	
	private static final long serialVersionUID = 1L;
	
	private final T left, right;
	private final double normalizedScore;
	private final Status status;
	
	AbstractDiff(double normalizedScore, Status status, T left, T right) {
		this.normalizedScore = normalizedScore;
		this.status = requireNonNull(status, "Status cannot be null");
		this.left = left;
		this.right = right;
	}
	
	AbstractDiff(Collection<? extends Diff<?>> diffs, Status status, T left, T right) {
		this(status.isChanged() ? diffs.stream().mapToDouble(Diff::getScore).average().orElse(0.0) : 1.0, status, left, right);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getLeft() {
		return left;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getRight() {
		return right;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getScore() {
		return normalizedScore;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status getStatus() {
		return status;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("%s[status = %s, score = %.3f, left = %s, right = %s]", getClass().getSimpleName(), status, normalizedScore, left, right);
	}
}
