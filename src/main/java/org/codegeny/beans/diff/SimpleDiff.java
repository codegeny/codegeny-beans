package org.codegeny.beans.diff;

/**
 * Implementation of {@link Diff} for simple values.
 *
 * @author Xavier DURY
 * @param <T> The type of the 2 compared objects.
 */
public final class SimpleDiff<T> extends AbstractDiff<T> {
	
	private static final long serialVersionUID = 1L;
	
	SimpleDiff(Status status, T left, T right) {
		this(status.isChanged() ? 0.0 : 1.0, status, left, right);
	}
	
	SimpleDiff(double score, Status status, T left, T right) {
		super(score, status, left, right);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(DiffVisitor<T, R> visitor) {
		return visitor.visitSimple(this);
	}
}