package org.codegeny.beans.model.visitor.diff;

import java.util.stream.IntStream;

import org.codegeny.beans.util.TimeOut;

/**
 * This implementation of score optimizer will find THE best solution, the one that globally optimizes the score.
 * 
 * @author Xavier DURY
 */
public class GlobalScoreOptimizer implements ScoreOptimizer {
	
	private final TimeOut timeOut;
	
	public GlobalScoreOptimizer(TimeOut timeOut) {
		this.timeOut = timeOut;
	}

	public int[] solve(int k, int n, double[][] matrix) {
		if (n < k || k <= 0) {
			throw new RuntimeException("0 < k <= n");
		}
		int[] bestSolution = new int[k];
		double bestScore = solve(k, n, matrix, new boolean[n], new int[k], 0, bestSolution, Double.NEGATIVE_INFINITY);
		// if the score is zero, this means that the solution vector is empty and some default solution should be returned
		return bestScore > 0 ? bestSolution : IntStream.range(0, k).toArray();
	}
	
	private double solve(int k, int n, double[][] matrix, boolean[] used, int[] currentSolution, double currentScore, int[] bestSolution, double bestScore) {
		if (currentScore + k > bestScore) { // if we use >= instead of >, we don't need to return a default solution above but this could take more time
			if (k-- == 0) {
				System.arraycopy(currentSolution, 0, bestSolution, 0, currentSolution.length);
				bestScore = currentScore;
			} else {
				this.timeOut.check();
				for (int i = 0; i < n; i++) {
					if (!used[i]) {
						used[i] = true;
						bestScore = solve(k, n, matrix, used, currentSolution, currentScore + matrix[k][currentSolution[k] = i], bestSolution, bestScore);
						used[i] = false;
					}
				}
			}
		}
		return bestScore;
	}
}