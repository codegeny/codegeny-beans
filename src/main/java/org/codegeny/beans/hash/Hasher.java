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

import java.util.Objects;

/**
 * Hasher interface.
 *
 * @author Xavier DURY
 */
public interface Hasher {

    /**
     * Hash a boolean.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(boolean value) {
        return hash(Boolean.hashCode(value));
    }

    /**
     * Hash a byte.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(byte value) {
        return hash(Byte.hashCode(value));
    }

    /**
     * Hash a char.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(char value) {
        return hash(Character.hashCode(value));
    }

    /**
     * Hash a double.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(double value) {
        return hash(Double.hashCode(value));
    }

    /**
     * Hash a float.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(float value) {
        return hash(Float.hashCode(value));
    }

    /**
     * Hash an int.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    Hasher hash(int value);

    /**
     * Hash a long.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(long value) {
        return hash(Long.hashCode(value));
    }

    /**
     * Hash an object.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(Object value) {
        return hash(Objects.hashCode(value));
    }

    /**
     * Hash a short.
     *
     * @param value The value to hash.
     * @return The current hasher.
     */
    default Hasher hash(short value) {
        return hash(Short.hashCode(value));
    }

    /**
     * Get the resulting hash value.
     *
     * @return The hash value.
     */
    int toHash();
}
