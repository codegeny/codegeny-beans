/*-
 * #%L
 * codegeny-beans
 * %%
 * Copyright (C) 2016 - 2018 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.codegeny.beans;

import org.codegeny.beans.model.Model;

import java.util.Objects;

import static org.codegeny.beans.model.Model.STRING;
import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Model.property;

public final class Address {

    public static final Model<Address> MODEL = bean(Address.class, //
            property("street", Address::getStreet, STRING), //
            property("zipCode", Address::getZipCode, STRING), //
            property("country", Address::getCountry, STRING) //
    );

    private final String country;
    private final String street;
    private final String zipCode;

    public Address(String street, String zipCode, String country) {
        this.street = street;
        this.zipCode = zipCode;
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public Address withCountry(String country) {
        return new Address(street, zipCode, country);
    }

    public String getStreet() {
        return street;
    }

    public Address withStreet(String street) {
        return new Address(street, zipCode, country);
    }

    public String getZipCode() {
        return zipCode;
    }

    public Address withZipCode(String zipCode) {
        return new Address(street, zipCode, country);
    }

    @Override
    public boolean equals(Object that) {
        return super.equals(that) || that instanceof Address && equals((Address) that);

    }

    private boolean equals(Address that) {
        return Objects.equals(country, that.country) && Objects.equals(street, that.street) && Objects.equals(zipCode, that.zipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, street, zipCode);
    }
}
