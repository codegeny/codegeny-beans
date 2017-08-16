package org.codegeny.beans.model.visitor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.util.AddAndXorHasher;

// TODO optimize this shit
public class DiffModelVisitor<T> extends CommonDiffModelVisitor<T> {
	
	private interface Adder<T> {
		
		void accept(int x, int y, Diff<T> diff);
	}
	
	protected final ScoreOptimizer optimizer;
	protected final double threshold;
	
	public DiffModelVisitor(T left, T right, double threshold, ScoreOptimizer optimizer) {
		super(left, right);
		this.threshold = threshold;
		this.optimizer = optimizer;
	}
	
	@Override
	protected <S> CommonDiffModelVisitor<S> newVisitor(S left, S right) {
		return new DiffModelVisitor<>(left, right, threshold, optimizer);
	}
	
	@Override
	public <E> Diff<T> visitList(ListModel<? super T, E> values) {
		List<? extends E> left = values.apply(super.left);
		List<? extends E> right = values.apply(super.right);
		List<Diff<E>> result = new LinkedList<>();
		boolean removeFirst = true;
		int i = 0, j = 0;
		int m = 0, n = 0;
		main:
		while (i < left.size() && j < right.size()) {
			E l = left.get(i), r = right.get(j);
			for (int a = i, b = j; a < left.size() || b < right.size(); a++, b++) {
				if (a < left.size()) {
					Diff<E> diff = values.acceptElement(newVisitor(left.get(a), r));
					if (diff.getScore() >= threshold) {
						if (removeFirst) {
							range(i - m, i = a).forEach(q -> result.add(values.acceptElement(new RemovedDiffModelVisitor<>(left.get(q)))));
							range(j - n, j).forEach(q -> result.add(values.acceptElement(new AddedDiffModelVisitor<>(right.get(q)))));
							removeFirst = n == 0;
						} else {
							range(j - n, j).forEach(q -> result.add(values.acceptElement(new AddedDiffModelVisitor<>(right.get(q)))));
							range(i - m, i = a).forEach(q -> result.add(values.acceptElement(new RemovedDiffModelVisitor<>(left.get(q)))));
							removeFirst = m > 0;
						}
						result.add(diff);
						i++;
						j++;
						m = n = 0;
						continue main;
					}
				}
				if (b < right.size()) { // TODO: do not compare initial values twice
					Diff<E> diff = values.acceptElement(newVisitor(l, right.get(b)));
					if (diff.getScore() >= threshold) {
						if (removeFirst) {
							range(i - m, i).forEach(q -> result.add(values.acceptElement(new RemovedDiffModelVisitor<>(left.get(q)))));
							range(j - n, j = b).forEach(q -> result.add(values.acceptElement(new AddedDiffModelVisitor<>(right.get(q)))));
							removeFirst = n == 0;
						} else {
							range(j - n, j = b).forEach(q -> result.add(values.acceptElement(new AddedDiffModelVisitor<>(right.get(q)))));
							range(i - m, i).forEach(q -> result.add(values.acceptElement(new RemovedDiffModelVisitor<>(left.get(q)))));
							removeFirst = m > 0;
						}
						result.add(diff);
						i++;
						j++;
						m = n = 0;
						continue main;
					}
				}
			}
			i++;
			j++;
			m++;
			n++;
		}
		if (removeFirst) {
			range(i - m, left.size()).forEach(q -> result.add(values.acceptElement(new RemovedDiffModelVisitor<>(left.get(q)))));
			range(j - n, right.size()).forEach(q -> result.add(values.acceptElement(new AddedDiffModelVisitor<>(right.get(q)))));
		} else {
			IntStream.range(j - n, right.size()).forEach(q -> result.add(values.acceptElement(new AddedDiffModelVisitor<>(right.get(q)))));
			IntStream.range(i - m, left.size()).forEach(q -> result.add(values.acceptElement(new RemovedDiffModelVisitor<>(left.get(q)))));
		}
		return Diff.list(toStatus(result), super.left, super.right, result);
	}
	
	public <E> Diff<T> visitSet(SetModel<? super T, E> values) {
		
		List<E> leftValues = new ArrayList<>(values.apply(super.left));
		List<E> rightValues = new ArrayList<>(values.apply(super.right));
		
		final int leftSize = leftValues.size();
		final int rightSize = rightValues.size();
		
		List<Diff<E>> list = new LinkedList<>();
		
		// used objects (all false to begin)
		
		boolean[] leftUsed = new boolean[leftSize];
		boolean[] rightUsed = new boolean[rightSize];
		
		// hash all objects to guide comparison (same hash = maybe same object)
		
		int[] leftHashes = leftValues.stream().mapToInt(e -> values.getElementModel().hash(e, new AddAndXorHasher())).toArray();
		int[] rightHashes = rightValues.stream().mapToInt(e -> values.getElementModel().hash(e, new AddAndXorHasher())).toArray();
		
		Adder<E> addBoth = (i, j, diff) -> {
			rightUsed[j] = leftUsed[i] = true;
			list.add(diff);
		};
		
		// if two objects have the same hash, compare them, if equal remove them from the list and add a diff
		
		for (int i = 0; i < leftSize; i++) {
			for (int j = 0; !leftUsed[i] && j < rightSize; j++) {
				if (!rightUsed[j] && leftHashes[i] == rightHashes[j]) {
					E left = leftValues.get(i);
					E right = rightValues.get(j);
					Diff<E> diff = values.acceptElement(newVisitor(left, right));
					if (!diff.getStatus().isChanged()) {
						addBoth.accept(i, j, diff);
						break;
					}
				}
			}
		}
		
		// for remaining, objects calculate a diff matrix for each possible pair
		
		@SuppressWarnings("unchecked")
		Diff<E>[][] matrix = new Diff[leftSize][rightSize];
		
		range(0, leftSize).filter(i -> !leftUsed[i]).forEach(i -> range(0, rightSize).filter(j -> !rightUsed[j]).forEach(j -> {
			E left = leftValues.get(i);
			E right = rightValues.get(j);
			Diff<E> diff = values.acceptElement(newVisitor(left, right));
			matrix[i][j] = diff.getScore() >= threshold ? diff : null;
		}));

		@SuppressWarnings("unchecked")
		Diff<E>[] leftNulls = new Diff[leftSize];
		
		@SuppressWarnings("unchecked")
		Diff<E>[] rightNulls = new Diff[rightSize];
		
		IntConsumer addLeft = i -> {
			leftUsed[i] = true;
			list.add(leftNulls[i]);
		};
		
		IntConsumer addRight = j -> {
			rightUsed[j] = true;
			list.add(rightNulls[j]);
		};

		// calculate left/null and right/null
		
		range(0, leftSize).filter(i -> !leftUsed[i]).forEach(i -> leftNulls[i] = values.getElementModel().accept(new RemovedDiffModelVisitor<>(leftValues.get(i)))); // left->null:REMOVED
		range(0, rightSize).filter(j -> !rightUsed[j]).forEach(j -> rightNulls[j] = values.getElementModel().accept(new AddedDiffModelVisitor<>(rightValues.get(j)))); // null->right:ADDED
		
		// if a column or row is full of nulls, remove it
		
		range(0, leftSize).filter(i -> !leftUsed[i]).filter(i -> range(0, rightSize).filter(j -> !rightUsed[j]).allMatch(j -> matrix[i][j] == null)).forEach(addLeft);
		range(0, rightSize).filter(j -> !rightUsed[j]).filter(j -> range(0, leftSize).filter(i -> !leftUsed[i]).allMatch(i -> matrix[i][j] == null)).forEach(addRight);
				
		// try to find the permutation that maximize the score
		
		int[] leftMapping = range(0, leftSize).filter(i -> !leftUsed[i]).toArray();
		int[] rightMapping = range(0, rightSize).filter(j -> !rightUsed[j]).toArray();
				
		if (leftMapping.length > 0 && rightMapping.length > 0) {
			
			BiFunction<Integer, Integer, Diff<E>> source = leftMapping.length < rightMapping.length
				? (a, b) -> matrix[leftMapping[a]][rightMapping[b]]
				: (a, b) -> matrix[leftMapping[b]][rightMapping[a]];
				
			double[][] scores = new double[min(rightMapping.length, leftMapping.length)][max(rightMapping.length, leftMapping.length)];
			for (int k = 0; k < min(rightMapping.length, leftMapping.length); k++) {
				for (int n = 0; n < max(rightMapping.length, leftMapping.length); n++) {
					Diff<E> diff = source.apply(k, n);
					scores[k][n] = diff != null ? diff.getScore() : 0; // TODO sum of both scores (0 + 0 for normalized, -x + -x for absolute)
 				}
			}
			
			int[] mapping = this.optimizer.solve(min(rightMapping.length, leftMapping.length), max(rightMapping.length, leftMapping.length), scores);
			range(0, mapping.length).forEach(i -> {
				int x = leftMapping[leftMapping.length >= rightMapping.length ? mapping[i] : i];
				int y = rightMapping[leftMapping.length >= rightMapping.length ? i : mapping[i]];
				Diff<E> diff = matrix[x][y];
				if (diff != null) {
					addBoth.accept(x, y, diff);
				} else {
					addLeft.accept(x);
					addRight.accept(y);
				}
			});
		}
		
		// the rest is the non-matching values
		
		range(0, leftSize).filter(i -> !leftUsed[i]).forEach(addLeft);
		range(0, rightSize).filter(j -> !rightUsed[j]).forEach(addRight);

		// sort the result
		
		Collections.sort(list, comparing(e -> e.getLeft() == null ? e.getRight() : e.getLeft(), nullsLast(new ModelComparator<>(values.getElementModel()))));
		return Diff.list(toStatus(list), left, right, list);
	}
}