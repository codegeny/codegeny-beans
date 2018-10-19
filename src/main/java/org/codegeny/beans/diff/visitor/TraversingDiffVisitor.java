package org.codegeny.beans.diff.visitor;

import static org.codegeny.beans.util.IndexedConsumer.forEachIndexed;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.codegeny.beans.diff.BeanDiff;
import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.DiffVisitor;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.diff.SimpleDiff;
import org.codegeny.beans.path.Path;

/**
 * Visitor which will traverse the whole tree.
 * 
 * @author Xavier DURY
 * @param <T> The diff'ed type.
 */
public final class TraversingDiffVisitor<T> implements DiffVisitor<T, Void> {
	
	private final Path<Object> path;
	private final BiPredicate<? super Path<?>, ? super Diff<?>> processor;

	public TraversingDiffVisitor(BiPredicate<? super Path<?>, ? super Diff<?>> processor) {
		this(Path.root(), processor);
	}
	
	public TraversingDiffVisitor(BiConsumer<? super Path<?>, ? super Diff<?>> processor) {
		this(Path.root(), (a, b) -> {
			processor.accept(a, b);
			return true;
		});
	}

	private TraversingDiffVisitor(Path<Object> path, BiPredicate<? super Path<?>, ? super Diff<?>> processor) {
		this.path = path;
		this.processor = processor;
	}
	
	private <R> TraversingDiffVisitor<R> childVisitor(Path<Object> path) {
		return new TraversingDiffVisitor<>(path, processor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void visitBean(BeanDiff<T> beanDiff) {
		if (processor.test(path, beanDiff)) {
			beanDiff.getProperties().forEach((n, p) -> p.accept(childVisitor(path.append(n))));
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E> Void visitList(ListDiff<T, E> listDiff) {
		if (processor.test(path, listDiff)) {
			forEachIndexed(listDiff.getList(), (i, n) -> n.accept(childVisitor(path.append(i))));
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> Void visitMap(MapDiff<T, K, V> mapDiff) {
		if (processor.test(path, mapDiff)) {
			mapDiff.getMap().forEach((k, v) -> v.accept(childVisitor(path.append(k))));
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void visitSimple(SimpleDiff<T> simpleDiff) {
		processor.test(path, simpleDiff);
		return null;
	}	
}
