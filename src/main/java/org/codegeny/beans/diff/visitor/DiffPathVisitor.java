package org.codegeny.beans.diff.visitor;

import org.codegeny.beans.diff.BeanDiff;
import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.path.PathVisitor;

public enum DiffPathVisitor implements PathVisitor<Diff<?>> {
	
	INSTANCE;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Diff<?> visitIndex(Diff<?> parent, int index) {
		assert parent instanceof ListDiff;
		return ((ListDiff<?, ?>) parent).getList().get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Diff<?> visitKey(Diff<?> parent, Object key) {
		assert parent instanceof MapDiff;
		return ((MapDiff<?, ?, ?>) parent).getMap().get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Diff<?> visitProperty(Diff<?> parent, String property) {
		assert parent instanceof BeanDiff;
		return ((BeanDiff<?>) parent).getProperties().get(property);
	}
}
