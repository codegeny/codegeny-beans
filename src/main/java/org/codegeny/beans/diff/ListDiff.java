package org.codegeny.beans.diff;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of <code>{@link Diff}</code> for lists.
 *
 * @author Xavier DURY
 * @param <L> The type of list.
 * @param <E> The type of list element.
 */
public final class ListDiff<L, E> extends AbstractDiff<L> {
	
	private static final long serialVersionUID = 1L;
	
	private final List<? extends Diff<E>> list;

	ListDiff(Status status, L left, L right, List<? extends Diff<E>> list) {
		super(list, status, left, right);
		this.list = Collections.unmodifiableList(list);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(DiffVisitor<L, R> visitor) {
		return visitor.visitList(this);
	}
	
	/**
	 * Get the list of diff'ed elements.
	 * 
	 * @return The list.
	 */
	public List<? extends Diff<E>> getList() {
		return list;
	}
}