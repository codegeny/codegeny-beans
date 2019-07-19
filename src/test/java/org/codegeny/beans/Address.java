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

import static org.codegeny.beans.model.Model.STRING;
import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Model.property;

public class Address {

    public static final Model<Address> MODEL = bean(Address.class, //
            property("street", Address::getStreet, Address::setStreet, STRING), //
            property("zipCode", Address::getZipCode, Address::setZipCode, STRING), //
            property("country", Address::getCountry, Address::setCountry, STRING) //
    );

    private String country;
    private String street;
    private String zipCode;

    public String getCountry() {
        return country;
    }

    public Address setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getStreet() {
        return street;
    }

    public Address setStreet(String street) {
        this.street = street;
        return this;
    }

    public String getZipCode() {
        return zipCode;
    }

    public Address setZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }
}
