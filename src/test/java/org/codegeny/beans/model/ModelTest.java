package org.codegeny.beans.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDate;

import org.codegeny.beans.Person;
import org.codegeny.beans.model.visitor.Typer;
import org.codegeny.beans.model.visitor.json.FromStringJsonDeserializer;
import org.codegeny.beans.model.visitor.json.JavaTypeModelVisitor;
import org.codegeny.beans.model.visitor.json.ToStringJsonSerializer;
import org.codegeny.beans.path.Path;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ModelTest {
	
	private static class JsonTyper implements Typer<String> {
		
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
	
	@Test
	public void extractPath() throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		mapper
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
	                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
	                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
	                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
	                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
	        )
		;
		
        SimpleModule testModule = new SimpleModule("MyModule", Version.unknownVersion());
        testModule.addSerializer(LocalDate.class, new ToStringJsonSerializer<>(LocalDate::toString));
        testModule.addDeserializer(LocalDate.class, new FromStringJsonDeserializer<>(LocalDate::parse));
        mapper.registerModule(testModule);

		Typer<String> typer = new JsonTyper(mapper);
		
		Person person = Person.createDefaultPerson();
		
		System.out.println(person.getMiddleNames());
		Person.MODEL.set(person, Path.of("middleNames", 0), "Fridrick");
		System.out.println(person.getMiddleNames());
		
//		
		Person.MODEL.set(person, Path.of("\"birthDate\""), "\"2018-01-01\"", typer);
		Person.MODEL.set(person, Path.of("\"middleNames\"", "0"), "\"Yannick\"", typer);
		
		System.out.println(Person.MODEL.get(person, Path.of("\"middleNames\"", "0"), typer));
		
		System.out.println(person.getMiddleNames());
		
		System.out.println(person.getBirthDate());
		
		
		System.out.println(person.getFirstName());
		Person.MODEL.set(person, Path.of("firstName"), "Jack");
		System.out.println(person.getFirstName());
		
		
		assertEquals("Grand Place", Person.MODEL.get(Person.createDefaultPerson(), Path.of("formerAddresses", 1, "street")));
	}
	
//	@Test
//	public void testToString() {
//		System.out.println(Person.MODEL.toString(Person.createDefaultPerson()));
//	}
//	
//	@Test
//	public void testToPath() {
//		Person.MODEL.accept(new TraversingModelVisitor<>(Person.createDefaultPerson(), (p, v) -> System.out.printf("%s = %s%n", p.toString("person"), v)));
//	}
}
