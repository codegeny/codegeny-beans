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
package org.codegeny.beans.path;

import java.lang.reflect.Type;

/**
 * Class which converts or casts objects to the given type.
 *
 * @param <S> The source object constrained type.
 * @author Xavier DURY
 */
public interface Converter<S> {

    /**
     * Identity converter which casts the value to the model type.
     */
    enum Identity implements Converter<Object> {

        /**
         * Singleton.
         */
        INSTANCE;

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Type type, Object source) {
            return (T) source;
        }
    }

    /**
     * Convert/cast the value to &gt;T&lt;
     *
     * @param type  The type.
     * @param value The value to convert.
     * @param <T>   The type of the model.
     * @return An object of type &gt;T&lt;
     */
    <T> T convert(Type type, S value);
}
