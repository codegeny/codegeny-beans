package org.codegeny.beans;

import static org.codegeny.beans.model.Model.STRING;
import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Property.immutable;

import org.codegeny.beans.model.Model;

public class Address {

	public static final Model<Address> MODEL = bean(Address.class, //
			immutable("street", Address::getStreet, STRING), //
			immutable("zipCode", Address::getZipCode, STRING), //
			immutable("country", Address::getCountry, STRING) //
	);

	private String country;
	private String street;
	private String zipCode;

	public String getCountry() {
		return country;
	}

	public String getStreet() {
		return street;
	}

	public String getZipCode() {
		return zipCode;
	}

	public Address setCountry(String country) {
		this.country = country;
		return this;
	}

	public Address setStreet(String street) {
		this.street = street;
		return this;
	}

	public Address setZipCode(String zipCode) {
		this.zipCode = zipCode;
		return this;
	}
}