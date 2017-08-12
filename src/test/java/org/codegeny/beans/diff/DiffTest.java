package org.codegeny.beans.diff;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.codegeny.beans.Person.MODEL;
import static org.codegeny.beans.Person.createDefaultPerson;
import static org.codegeny.beans.diff.Diff.Status.ADDED;
import static org.codegeny.beans.diff.Diff.Status.MODIFIED;
import static org.codegeny.beans.diff.Diff.Status.REMOVED;
import static org.codegeny.beans.diff.Diff.Status.UNCHANGED;
import static org.codegeny.beans.path.Path.path;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.codegeny.beans.Person;
import org.codegeny.beans.model.visitor.DiffModelVisitor;
import org.codegeny.beans.model.visitor.GlobalScoreOptimizer;
import org.codegeny.beans.util.TimeOut;
import org.junit.Test;

public class DiffTest {
	
	@Test
	public void identicalObjectsShouldYieldNoDifferences() {
		Person person = createDefaultPerson();
		Diff<Person> diff = MODEL.accept(new DiffModelVisitor<>(person, person, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(UNCHANGED));
	}
	
	@Test
	public void sameObjectsShouldYieldNoDifferences() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson();
		Diff<Person> diff = MODEL.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(UNCHANGED));
	}
	
	@Test
	public void objectsWithOneDifferentPropertyShouldYieldOneDiffrence() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().setFirstName("Jack");
		Diff<Person> diff = MODEL.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}
	
	@Test
	public void objectsWithOnePropertySetToNullShouldYieldOneDifference() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().setFirstName(null);
		Diff<Person> diff = MODEL.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}
	
	@Test
	public void objectsWithOnePropertyBeanSetToNullShouldYieldOneDifference() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().setCurrentAddress(null);
		Diff<Person> diff = MODEL.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress").property("street")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress").property("zipCode")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("currentAddress").property("country")).getStatus(), is(REMOVED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}
	
	@Test
	public void test50() {
		Person left = createDefaultPerson();
		Person right = createDefaultPerson().addMiddleName("Michael").removeMiddleName(middleName -> middleName.startsWith("Fitz"));
		Diff<Person> diff = MODEL.accept(new DiffModelVisitor<>(left, right, 0.5, new GlobalScoreOptimizer(new TimeOut(5, SECONDS))));
		assertThat(diff.getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("birthDate")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("lastName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("middleNames")).getStatus(), is(MODIFIED));
		assertThat(diff.extract(path().property("middleNames").index(0)).getStatus(), is(REMOVED)); // Fitzgerald
		assertThat(diff.extract(path().property("middleNames").index(1)).getStatus(), is(ADDED)); // Michael
		assertThat(diff.extract(path().property("middleNames").index(2)).getStatus(), is(UNCHANGED)); // Patrick
		assertThat(diff.extract(path().property("firstName")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("currentAddress")).getStatus(), is(UNCHANGED));
		assertThat(diff.extract(path().property("formerAddresses")).getStatus(), is(UNCHANGED));
	}
}
