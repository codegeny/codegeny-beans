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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.codegeny.beans.model.Model.STRING;
import static org.codegeny.beans.model.Model.bean;
import static org.codegeny.beans.model.Model.list;
import static org.codegeny.beans.model.Model.map;
import static org.codegeny.beans.model.Model.property;
import static org.codegeny.beans.model.Model.set;
import static org.codegeny.beans.model.Model.value;
import static org.codegeny.beans.model.Property.list;
import static org.codegeny.beans.model.Property.set;

public class Person {

    public static final Model<Person> MODEL = bean(Person.class, //
            property("firstName", Person::getFirstName, Person::setFirstName, STRING), //
            property("middleNames", list(Person::getMiddleNames, p -> i -> e -> p.setMiddleName(i, e)), list(STRING)), //
            property("lastName", Person::getLastName, STRING), //
            property("birthDate", Person::getBirthDate, Person::setBirthDate, value(LocalDate.class)), //
            property("currentAddress", Person::getCurrentAddress, Address.MODEL), //
            property("formerAddresses", set(Person::getFormerAddresses, Person::addFormerAddress), set(Address.MODEL)), //
            property("hints", Person::getHints, map(STRING, STRING)) //
    );

    private LocalDate birthDate;
    private Address currentAddress;
    private String firstName;
    private Set<Address> formerAddresses = new LinkedHashSet<>();
    private String lastName;
    private List<String> middleNames = new LinkedList<>();
    private Map<String, String> hints = new LinkedHashMap<>();

    public static Person createDefaultPerson() {
        return new Person() //
                .setBirthDate(LocalDate.now()) //
                .setFirstName("John") //
                .addMiddleName("Patrick") //
                .addMiddleName("Fitzgerald") //
                .setLastName("Doe") //
                .setCurrentAddress(new Address("Evergreen Terrasse", "90210", "USA")) //
                .addFormerAddress(new Address("Champs Elysées", "1000", "France")) //
                .addFormerAddress(new Address("Grand Place", "1000", "Belgium"));
    }

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

    public Person setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public Address getCurrentAddress() {
        return currentAddress;
    }

    public Person setCurrentAddress(Address currentAddress) {
        this.currentAddress = currentAddress;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Person setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Set<Address> getFormerAddresses() {
        return formerAddresses;
    }

    public String getLastName() {
        return lastName;
    }

    public Person setLastName(String lastName) {
        this.lastName = lastName;
        return this;
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

    public Map<String, String> getHints() {
        return hints;
    }
}
