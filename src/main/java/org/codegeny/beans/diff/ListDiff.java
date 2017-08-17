package org.codegeny.beans.diff;

import static java.util.Collections.unmodifiableList;

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
	
	private final List<Diff<E>> list;

	ListDiff(Status status, L left, L right, List<? extends Diff<E>> list) {
		super(list, status, left, right);
		this.list = unmodifiableList(list);
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
	public List<Diff<E>> getList() {
		return list;
	}
}