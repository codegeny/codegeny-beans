package org.codegeny.beans.model.visitor.json;

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