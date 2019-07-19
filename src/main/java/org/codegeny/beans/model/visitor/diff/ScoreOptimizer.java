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

/**
 * A score optimizer takes a matrix of scores and tries to pair row elements with
 * column elements based on their matching scores.
 */
public interface ScoreOptimizer {

    /**
     * Solve the score.
     *
     * @param k      The number of rows.
     * @param n      The number of columns which must be greater or equals to k.
     * @param matrix A matrix (n:k) of normalized scores in [0; 1]
     * @return An array of size k which gives for each row (k), the index of the paired column (n).
     */
    int[] solve(int k, int n, double[][] matrix);
}
