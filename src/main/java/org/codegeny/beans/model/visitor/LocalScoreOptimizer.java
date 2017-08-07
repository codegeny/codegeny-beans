package org.codegeny.beans.model.visitor;

/**
 * This implementation will find the successive best local scores, the result
 * won't necessary be the best global solution but one that is good enough in
 * less time.
 * 
 * @author Xavier DURY
 */
public class LocalScoreOptimizer implements ScoreOptimizer {

	// TODO prettify
	public int[] solve(int k, int n, double[][] matrix) {
		boolean[] usedN = new boolean[n], usedK = new boolean[k];
		int[] bestSolution = new int[k];
		double bestScore, score;
		for (int i = 0, bestK = -1, bestN = -1, a, b; i < k; i++) {
			bestScore = Double.NEGATIVE_INFINITY;
			for (a = 0; a < k; a++) {
				if (!usedK[a]) {
					for (b = 0; b < n; b++) {
						if (!usedN[b]) {
							score = matrix[a][b];
							if (score > bestScore) {
								bestK = a;
								bestN = b;
								bestScore = score;
							}
						}
					}
				}
			}
			usedN[bestN] = true;
			usedK[bestK] = true;
			bestSolution[bestK] = bestN;
		}
		return bestSolution;
	}
}