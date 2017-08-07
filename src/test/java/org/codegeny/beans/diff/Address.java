package org.codegeny.beans.diff;

public class Address {
	
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