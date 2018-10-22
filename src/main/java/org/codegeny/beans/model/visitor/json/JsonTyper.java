package org.codegeny.beans.model.visitor.json;

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

import java.io.IOException;

import org.codegeny.beans.model.Model;
import org.codegeny.beans.model.visitor.Typer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTyper implements Typer<String> {
	
	private final ObjectMapper mapper;

	public JsonTyper(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public <T> T retype(Model<T> model, String value) {
		try {
			return mapper.readerFor(model.accept(new JavaTypeModelVisitor<>(mapper.getTypeFactory()))).readValue(value);
		} catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}
}
