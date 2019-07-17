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
package org.codegeny.beans.model;

import org.codegeny.beans.Person;
import org.codegeny.beans.model.visitor.TraversingModelVisitor;
import org.codegeny.beans.model.visitor.TypeModelVisitor;
import org.codegeny.beans.path.Path;
import org.junit.jupiter.api.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTest {

    @Test
    public void extractPath() {

        Jsonb jsonb = JsonbBuilder.create();

        Typer<String> jsonTyper = new Typer<String>() {

            @Override
            public <T> T retype(Model<T> model, String value) {
                return jsonb.fromJson(value, model.accept(new TypeModelVisitor<>()));
            }
        };

        Person person = Person.createDefaultPerson();

        assertEquals(Arrays.asList("Patrick", "Fitzgerald"), person.getMiddleNames());
        Person.MODEL.set(person, Path.of("middleNames", 0), "Fridrick");
        assertEquals(Arrays.asList("Fridrick", "Fitzgerald"), person.getMiddleNames());

        Person.MODEL.set(person, Path.of("\"middleNames\"", "0"), "\"Yannick\"", jsonTyper);
        assertEquals(Arrays.asList("Yannick", "Fitzgerald"), person.getMiddleNames());

        Person.MODEL.set(person, Path.of("\"birthDate\""), "\"2018-01-01\"", jsonTyper);
        assertEquals(LocalDate.of(2018, 1, 1), person.getBirthDate());

        assertEquals("John", person.getFirstName());
        Person.MODEL.set(person, Path.of("firstName"), "Jack");
        assertEquals("Jack", person.getFirstName());

        assertEquals("Grand Place", Person.MODEL.get(Person.createDefaultPerson(), Path.of("formerAddresses", 1, "street")));
    }

    @Test
    public void testToString() {
        System.out.println(Person.MODEL.toString(Person.createDefaultPerson()));
    }

    @Test
    public void testToPath() {
        Person.MODEL.accept(new TraversingModelVisitor<>(Person.createDefaultPerson(), (p, v) -> System.out.printf("%s = %s%n", p, v)));
    }

    @Test
    public void describe() {
        System.out.println(Person.MODEL.describe());
    }
}
