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
package org.codegeny.beans.diff;

import org.codegeny.beans.Person;
import org.codegeny.beans.model.visitor.diff.DefaultComputeDiffModelVisitor;
import org.codegeny.beans.model.visitor.diff.GlobalScoreOptimizer;
import org.codegeny.beans.path.Path;
import org.junit.jupiter.api.Test;

import static org.codegeny.beans.Person.MODEL;
import static org.codegeny.beans.Person.createDefaultPerson;
import static org.codegeny.beans.diff.Diff.Status.ADDED;
import static org.codegeny.beans.diff.Diff.Status.MODIFIED;
import static org.codegeny.beans.diff.Diff.Status.REMOVED;
import static org.codegeny.beans.diff.Diff.Status.UNCHANGED;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiffTest {

    @Test
    public void identicalObjectsShouldYieldNoDifferences() {
        Person person = createDefaultPerson();
        Diff<Person> diff = MODEL.accept(new DefaultComputeDiffModelVisitor<>(person, person, 0.5, new GlobalScoreOptimizer()));
        assertEquals(UNCHANGED, diff.getStatus());
    }

    @Test
    public void sameObjectsShouldYieldNoDifferences() {
        Person left = createDefaultPerson();
        Person right = createDefaultPerson();
        Diff<Person> diff = MODEL.accept(new DefaultComputeDiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer()));
        assertEquals(UNCHANGED, diff.getStatus());
    }

    @Test
    public void objectsWithOneDifferentPropertyShouldYieldOneDiffrence() {
        Person left = createDefaultPerson();
        Person right = createDefaultPerson().setFirstName("Jack");
        Diff<Person> diff = MODEL.accept(new DefaultComputeDiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer()));
        assertEquals(MODIFIED, diff.getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("birthDate")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("lastName")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("middleNames")).getStatus());
        assertEquals(MODIFIED, diff.get(Path.of("firstName")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("currentAddress")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("formerAddresses")).getStatus());
    }

    @Test
    public void objectsWithOnePropertySetToNullShouldYieldOneDifference() {
        Person left = createDefaultPerson();
        Person right = createDefaultPerson().setFirstName(null);
        Diff<Person> diff = MODEL.accept(new DefaultComputeDiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer()));
        assertEquals(MODIFIED, diff.getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("birthDate")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("lastName")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("middleNames")).getStatus());
        assertEquals(REMOVED, diff.get(Path.of("firstName")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("currentAddress")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("formerAddresses")).getStatus());
    }

    @Test
    public void objectsWithOnePropertyBeanSetToNullShouldYieldOneDifference() {
        Person left = createDefaultPerson();
        Person right = createDefaultPerson().setCurrentAddress(null);
        Diff<Person> diff = MODEL.accept(new DefaultComputeDiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer()));
        assertEquals(MODIFIED, diff.getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("birthDate")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("lastName")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("middleNames")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("firstName")).getStatus());
        assertEquals(REMOVED, diff.get(Path.of("currentAddress")).getStatus());
        assertEquals(REMOVED, diff.get(Path.of("currentAddress", "street")).getStatus());
        assertEquals(REMOVED, diff.get(Path.of("currentAddress", "zipCode")).getStatus());
        assertEquals(REMOVED, diff.get(Path.of("currentAddress", "country")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("formerAddresses")).getStatus());
    }

    @Test
    public void test50() {
        Person left = createDefaultPerson();
        Person right = createDefaultPerson() //
                .addMiddleName("Michael") //
                .removeMiddleName(middleName -> middleName.startsWith("Fitz"));

        Diff<Person> diff = MODEL.accept(new DefaultComputeDiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer()));
        assertEquals(MODIFIED, diff.getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("birthDate")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("lastName")).getStatus());
        assertEquals(MODIFIED, diff.get(Path.of("middleNames")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("middleNames", 0)).getStatus()); // Patrick
        assertEquals(REMOVED, diff.get(Path.of("middleNames", 1)).getStatus()); // -Fitzgerald
        assertEquals(ADDED, diff.get(Path.of("middleNames", 2)).getStatus()); // +Michael
        assertEquals(UNCHANGED, diff.get(Path.of("firstName")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("currentAddress")).getStatus());
        assertEquals(UNCHANGED, diff.get(Path.of("formerAddresses")).getStatus());
    }
}
