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

public interface Hasher {
	
	default Hasher hash(boolean value) {
		return hash(Boolean.hashCode(value));
	}
	
	default Hasher hash(byte value) {
		return hash(Byte.hashCode(value));
	}
	
	default Hasher hash(char value) {
		return hash(Character.hashCode(value));
	}
	
	default Hasher hash(double value) {
		return hash(Double.hashCode(value));
	}
	
	default Hasher hash(float value) {
		return hash(Float.hashCode(value));
	}
	
	Hasher hash(int value);
	
	default Hasher hash(long value) {
		return hash(Long.hashCode(value));
	}
	
	default Hasher hash(Object... values) {
		return hash(Objects.hashCode(values));
	}
	
	default Hasher hash(short value) {
		return hash(Short.hashCode(value));
	}
	
	int toHash();
}
