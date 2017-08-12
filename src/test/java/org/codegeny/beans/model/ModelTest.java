package org.codegeny.beans.model;

import static org.codegeny.beans.path.Path.path;
import static org.junit.Assert.assertEquals;

import org.codegeny.beans.Person;
import org.codegeny.beans.model.visitor.TraversingModelVisitor;
import org.junit.Test;

public class ModelTest {
	
	@Test
	public void extractPath() {
		assertEquals("Grand Place", Person.MODEL.extract(Person.createDefaultPerson(), path().property("formerAddresses").index(1).property("street")));
	}
	
	@Test
	public void testToString() {
		System.out.println(Person.MODEL.toString(Person.createDefaultPerson()));
	}
	
	@Test
	public void testToPath() {
		Person.MODEL.accept(new TraversingModelVisitor<>(Person.createDefaultPerson(), (p, v) -> System.out.printf("%s = %s%n", p.toString("person"), v)));
	}
}
