/*-
 * #%L
 * codegeny-beans
 * %%
 * Copyright (C) 2016 - 2018 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.codegeny.beans.model.visitor.diff;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * This implementation of score optimizer will find THE best solution, the one that globally optimizes the score.
 * This implementation is not reusable. A timeout can be specified and an exception will thrown if it's exceeded.
 *
 * @author Xavier DURY
 */
public final class GlobalScoreOptimizer implements ScoreOptimizer {

    /**
     * How many check before actually enforcing the timeout.
     */
    private static final long COUNTER = 1000000;

    /**
     * System time before time out.
     */
    private final long limit;

    /**
     * The current counter. When it reaches 0, timeout is checked.
     */
    private long counter = COUNTER;

    /**
     * Default constructor with a 10s timeout.
     */
    public GlobalScoreOptimizer() {
        this(10, TimeUnit.SECONDS);
    }

    /**
     * Constructor.
     *
     * @param duration The timeout duration.
     * @param unit     The timeout unit.
     */
    public GlobalScoreOptimizer(long duration, TimeUnit unit) {
        this.limit = System.currentTimeMillis() + unit.toMillis(duration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] solve(int k, int n, double[][] matrix) {
        if (n < k || k <= 0) {
            throw new RuntimeException("0 < k <= n");
        }
        int[] bestSolution = new int[k];
        double bestScore = solve(k, n, matrix, new boolean[n], new int[k], 0, bestSolution, Double.NEGATIVE_INFINITY);
        // if the score is zero, this means that the solution vector is empty and some default solution should be returned
        return bestScore > 0 ? bestSolution : IntStream.range(0, k).toArray();
    }

    /**
     * Recursively try all permutations.
     *
     * @param k               The width/columns of the matrix.
     * @param n               The height/rows of the matrix.
     * @param matrix          The matrix.
     * @param used            Used rows.
     * @param currentSolution The vector (k) with the current solution.
     * @param currentScore    The current score.
     * @param bestSolution    The vector (k) with the best solution so fat.
     * @param bestScore       The best score so far.
     * @return The best score.
     */
    private double solve(int k, int n, double[][] matrix, boolean[] used, int[] currentSolution, double currentScore, int[] bestSolution, double bestScore) {
        if (currentScore + k > bestScore) { // if we use >= instead of >, we don't need to return a default solution above but this could take more time
            if (k-- == 0) {
                System.arraycopy(currentSolution, 0, bestSolution, 0, currentSolution.length);
                bestScore = currentScore;
            } else {
                check();
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

    /**
     * Check if the timeout is exceeded.
     */
    private void check() {
        if (--counter == 0) {
            counter = COUNTER;
            if (this.limit < System.currentTimeMillis()) {
                throw new RuntimeException("TIMEOUT!");
            }
        }
    }
}
