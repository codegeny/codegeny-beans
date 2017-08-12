package org.codegeny.beans.diff;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link Diff} for lists.
 *
 * @author Xavier DURY
 * @param <C> The type of list.
 * @param <E> The type of list element.
 */
public final class ListDiff<C, E> extends AbstractDiff<C> {
	
	private static final long serialVersionUID = 1L;
	
	private final List<? extends Diff<E>> list;

	ListDiff(Status status, C left, C right, List<? extends Diff<E>> list) {
		super(list, status, left, right);
		this.list = Collections.unmodifiableList(list);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(DiffVisitor<C, R> visitor) {
		return visitor.visitList(this);
	}
	
	/**
	 * Get the list of diffed elements.
	 * 
	 * @return The list.
	 */
	public List<? extends Diff<E>> getList() {
		return list;
	}
}