package org.codegeny.beans.diff;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.codegeny.beans.diff.Diff.Status.ADDED;
import static org.codegeny.beans.diff.Diff.Status.MODIFIED;
import static org.codegeny.beans.diff.Diff.Status.REMOVED;
import static org.codegeny.beans.diff.Diff.Status.UNCHANGED;
import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Model.set;
import static org.codegeny.beans.model.Model.value;
import static org.codegeny.beans.path.Path.path;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;

import org.codegeny.beans.model.Model;
import org.codegeny.beans.model.visitor.DiffModelVisitor;
import org.codegeny.beans.model.visitor.GlobalScoreOptimizer;
import org.codegeny.beans.util.TimeOut;
import org.junit.Test;

public class DiffTest {
	
	public static final Model<Address> ADDRESS;
	public static final Model<Person> PERSON;
	
	static {
		ADDRESS = bean()
			.addProperty("street", Address::getStreet, value())
			.addProperty("zipCode", Address::getZipCode, value())
			.addProperty("country", Address::getCountry, value());
		PERSON = bean()
			.addProperty("firstName", Person::getFirstName, value())
			.addProperty("middleNames", Person::getMiddleNames, set(value()))
			.addProperty("lastName", Person::getLastName, value()) //<1> 
			.addProperty("birthDate", Person::getBirthDate, value())
			.addProperty("currentAddress", Person::getCurrentAddress, ADDRESS)
			.addProperty("formerAddresses", Person::getFormerAddresses, set(ADDRESS));
	}
	
	public @Test void extractPath() {
		System.out.println(PERSON.extract(createDefaultPerson(), path().property("formerAddresses").index(1).property("street")));
	}
	
	public @Test void identicalObjectsShouldYieldNoDifferences() {
		Person person = createDefaultPerson();
		Diff<Person> diff = PERSON.accept(new DiffModelVisitor<>(person, person, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(UNCHANGED));
	}
	
	public @Test void sameObjectsShouldYieldNoDifferences() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson();
		Diff<Person> diff = PERSON.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(UNCHANGED));
	}
	
	public @Test void objectsWithOneDifferentPropertyShouldYieldOneDiffrence() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().setFirstName("Jack");
		Diff<Person> diff = PERSON.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}
	
	public @Test void objectsWithOnePropertySetToNullShouldYieldOneDifference() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().setFirstName(null);
		Diff<Person> diff = PERSON.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}
	
	public @Test void objectsWithOnePropertyBeanSetToNullShouldYieldOneDifference() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().setCurrentAddress(null);
		Diff<Person> diff = PERSON.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress").property("street")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress").property("zipCode")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress").property("country")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}
	
	public @Test void test50() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().addMiddleName("Michael").removeMiddleName(middleName -> middleName.startsWith("Fitz"));
		Diff<Person> diff = PERSON.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("middleNames").index(0)).getStatus(), is(REMOVED)); // Fitzgerald
		assertThat(diff.extract(path().property("middleNames").index(1)).getStatus(), is(ADDED)); // Michael
		assertThat(diff.extract(path().property("middleNames").index(2)).getStatus(), is(UNCHANGED)); // Patrick
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}

	private Person createDefaultPerson() {
		return new Person()
			.setBirthDate(LocalDate.now())
			.setFirstName("John")
			.addMiddleName("Patrick")
			.addMiddleName("Fitzgerald")
			.setLastName("Doe")
			.setCurrentAddress(new Address().setStreet("Evergreen Terrasse").setZipCode("90210").setCountry("USA"))
			.addFormerAddress(new Address().setStreet("Champs Elysées").setZipCode("1000").setCountry("France"))
			.addFormerAddress(new Address().setStreet("Grand Place").setZipCode("1000").setCountry("Belgium"));
	}
}
