package org.codegeny.beans.diff.visitor;

import static org.codegeny.beans.util.IndexedConsumer.forEachIndexed;

import java.util.function.BiConsumer;

import org.codegeny.beans.diff.BeanDiff;
import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.DiffVisitor;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.path.Path;

/**
 * TODO
 * 
 * @author Xavier DURY
 * @param <T> TODO
 */
public final class TraversingDiffVisitor<T> implements DiffVisitor<T, Void> {
	
	private final Path path;
	private final BiConsumer<? super Path, ? super Diff<?>> processor;

	public TraversingDiffVisitor(BiConsumer<? super Path, ? super Diff<?>> processor) {
		this(Path.path(), processor);
	}

	private TraversingDiffVisitor(Path path, BiConsumer<? super Path, ? super Diff<?>> processor) {
		this.path = path;
		this.processor = processor;
	}
	
	private <R> TraversingDiffVisitor<R> childVisitor(Path path) {
		return new TraversingDiffVisitor<>(path, processor);
	}

	private void process(Diff<T> diff) {
		this.processor.accept(this.path, diff);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void visitBean(BeanDiff<T> beanDiff) {
		process(beanDiff);
		beanDiff.getProperties().forEach((n, p) -> p.accept(childVisitor(path.property(n))));
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E> Void visitList(ListDiff<T, E> listDiff) {
		process(listDiff);
		forEachIndexed(listDiff.getList(), (i, n) -> n.accept(childVisitor(path.index(i)))); 
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> Void visitMap(MapDiff<T, K, V> mapDiff) {
		process(mapDiff);
		mapDiff.getMap().forEach((k, v) -> v.accept(childVisitor(path.key(k))));
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void visitSimple(SimpleDiff<T> simpleDiff) {
		process(simpleDiff);
		return null;
	}
}
