package org.codegeny.beans.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;

import org.codegeny.beans.Person;
import org.codegeny.beans.model.visitor.TraversingModelVisitor;
import org.codegeny.beans.model.visitor.Typer;
import org.codegeny.beans.model.visitor.json.FromStringJsonDeserializer;
import org.codegeny.beans.model.visitor.json.JsonTyper;
import org.codegeny.beans.model.visitor.json.ToStringJsonSerializer;
import org.codegeny.beans.path.Path;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ModelTest {
		
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
		
		assertEquals(Arrays.asList("Patrick", "Fitzgerald"), person.getMiddleNames());
		Person.MODEL.set(person, Path.of("middleNames", 0), "Fridrick");
		assertEquals(Arrays.asList("Fridrick", "Fitzgerald"), person.getMiddleNames());
		
		Person.MODEL.set(person, Path.of("\"middleNames\"", "0"), "\"Yannick\"", typer);
		assertEquals(Arrays.asList("Yannick", "Fitzgerald"), person.getMiddleNames());
		
		Person.MODEL.set(person, Path.of("\"birthDate\""), "\"2018-01-01\"", typer);
		assertEquals(LocalDate.of(2018, 1, 1), person.getBirthDate());
		
		assertEquals("John", person.getFirstName());
		Person.MODEL.set(person, Path.of("firstName"), "Jack");
		assertEquals("Jack", person.getFirstName());
		
		assertEquals("Grand Place", Person.MODEL.get(Person.createDefaultPerson(), Path.of("formerAddresses", 1, "street")));
	}
	
	@Test
	public void testToString() {
		System.out.println(Person.MODEL.toString(Person.createDefaultPerson()));
	}

	@Test
	public void testToPath() {
		Person.MODEL.accept(new TraversingModelVisitor<>(Person.createDefaultPerson(), (p, v) -> System.out.printf("%s = %s%n", p, v)));
	}
}
