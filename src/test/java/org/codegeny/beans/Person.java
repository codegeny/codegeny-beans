package org.codegeny.beans;

import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Model.set;
import static org.codegeny.beans.model.Model.value;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.codegeny.beans.model.Model;

public class Person {
	
	public static final Model<Person> MODEL = bean() //
		.addProperty("firstName", Person::getFirstName, value()) //
		.addProperty("middleNames", Person::getMiddleNames, set(value())) //
		.addProperty("lastName", Person::getLastName, value()) //
		.addProperty("birthDate", Person::getBirthDate, value()) //
		.addProperty("currentAddress", Person::getCurrentAddress, Address.MODEL) //
		.addProperty("formerAddresses", Person::getFormerAddresses, set(Address.MODEL));
	
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
	private Set<String> middleNames = new LinkedHashSet<>();
	
	public Person addFormerAddress(Address formerAddress) {
		formerAddresses.add(formerAddress);
		return this;
	}
	
	public Person addMiddleName(String middleName) {
		middleNames.add(middleName);
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
	
	public Set<String> getMiddleNames() {
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