package org.codegeny.beans;

import static org.codegeny.beans.model.Model.STRING;
import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Model.list;
import static org.codegeny.beans.model.Model.set;
import static org.codegeny.beans.model.Model.value;
import static org.codegeny.beans.model.Properties.list;
import static org.codegeny.beans.model.Properties.set;
import static org.codegeny.beans.model.Property.immutable;
import static org.codegeny.beans.model.Property.mutable;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.codegeny.beans.model.Model;

public class Person {

	public static final Model<Person> MODEL = bean(Person.class, //
			mutable("firstName", Person::getFirstName, Person::setFirstName, STRING), //
			immutable("middleNames", list(Person::getMiddleNames, p -> i -> e -> p.setMiddleName(i, e)), list(STRING)), //
			immutable("lastName", Person::getLastName, STRING), //
			mutable("birthDate", Person::getBirthDate, Person::setBirthDate, value(LocalDate.class)), //
			immutable("currentAddress", Person::getCurrentAddress, Address.MODEL), //
			immutable("formerAddresses", set(Person::getFormerAddresses, Person::addFormerAddress), set(Address.MODEL)) //
	);

	public static Person createDefaultPerson() {
		return new Person() //
				.setBirthDate(LocalDate.now()) //
				.setFirstName("John") //
				.addMiddleName("Patrick") //
				.addMiddleName("Fitzgerald") //
				.setLastName("Doe") //
				.setCurrentAddress(new Address().setStreet("Evergreen Terrasse").setZipCode("90210").setCountry("USA")) //
				.addFormerAddress(new Address().setStreet("Champs Elysées").setZipCode("1000").setCountry("France")) //
				.addFormerAddress(new Address().setStreet("Grand Place").setZipCode("1000").setCountry("Belgium"));
	}

	private LocalDate birthDate;
	private Address currentAddress;
	private String firstName;
	private Set<Address> formerAddresses = new LinkedHashSet<>();
	private String lastName;
	private List<String> middleNames = new LinkedList<>();

	public Person addFormerAddress(Address formerAddress) {
		formerAddresses.add(formerAddress);
		return this;
	}

	public Person addMiddleName(String middleName) {
		middleNames.add(middleName);
		return this;
	}
	
	public Person setMiddleName(int index, String middleName) {
		middleNames.set(index, middleName);
		return this;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public Address getCurrentAddress() {
		return currentAddress;
	}

	public String getFirstName() {
		return firstName;
	}

	public Set<Address> getFormerAddresses() {
		return formerAddresses;
	}

	public String getLastName() {
		return lastName;
	}

	public List<String> getMiddleNames() {
		return middleNames;
	}

	public Person removeFormerAddress(Predicate<? super Address> predicate) {
		formerAddresses.removeIf(predicate);
		return this;
	}

	public Person removeMiddleName(Predicate<? super String> predicate) {
		middleNames.removeIf(predicate);
		return this;
	}

	public Person setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
		return this;
	}

	public Person setCurrentAddress(Address currentAddress) {
		this.currentAddress = currentAddress;
		return this;
	}

	public Person setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public Person setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
}