package org.codegeny.beans.diff;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

public class Person {
	
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