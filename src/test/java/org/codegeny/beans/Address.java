package org.codegeny.beans;

import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Model.value;

import org.codegeny.beans.model.Model;

public class Address {
	
	public static final Model<Address> MODEL = bean() //
		.addProperty("street", Address::getStreet, value()) //
		.addProperty("zipCode", Address::getZipCode, value()) //
		.addProperty("country", Address::getCountry, value());
	
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