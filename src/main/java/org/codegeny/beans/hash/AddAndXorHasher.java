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
package org.codegeny.beans.hash;

/**
 * This implementation of hasher is not affected by the order of elements which are hashed.
 *
 * @author Xavier DURY
 */
public final class AddAndXorHasher implements Hasher {

    /**
     * The mask to use to combineWith xor and add values.
     */
    private static final int MASK = 0xaaaaaaaa;

    /**
     * The internal hashed values.
     */
    private int add, xor;

    /**
     * {@inheritDoc}
     */
    @Override
    public Hasher hash(int value) {
        this.add += value;
        this.xor ^= value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int toHash() {
        return (MASK & this.add) | (~MASK & this.xor);
    }
}
