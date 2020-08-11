![Status experimental](https://img.shields.io/badge/status-experimental-red.svg)
[![Build Status](https://img.shields.io/travis/codegeny/codegeny-beans.svg)](https://travis-ci.org/codegeny/codegeny-beans)
[![Maven Central](https://img.shields.io/maven-central/v/org.codegeny/codegeny-beans.svg)](https://mvnrepository.com/artifact/org.codegeny/codegeny-beans)
[![Code Coverage](https://img.shields.io/codecov/c/github/codegeny/codegeny-beans.svg)](https://codecov.io/gh/codegeny/codegeny-beans)
[![Code Analysis](https://img.shields.io/codacy/grade/2a447b2e20e34b628cef941f7619e184.svg)](https://www.codacy.com/app/codegeny/codegeny-beans)

# codegeny-beans

Express your bean structure in a reflection-free, dependency-free and type-safe model; get generic diff/comparison/toString/toJson...

This project makes heavy (maybe too much) use of the visitor pattern to avoid casting where possible.

## The Model interface

A `Model` is used to express the hierarchical structure of an object in a type-safe and reflection-free way.

In turn, once you have your model defined, you can use it to generate a toString for your object, compare objects, diff objects, hash objects... 

The `Model<T>` interface represents a node in the hierarchical structure for an object of type `T` and can be of one of the following concrete classes:

- `ValueModel<T>` which represents an atomic value of type `T`. Atomic values must be comparable or a comparator must be given to the `ValueModel`.
- `ListModel<L, E>` represents a list-like value (of type `L`) of other models (of type `E`). Note that `L` is not required to be of type `List<E>` but then a transformation function (`Function<L, List<E>>`) must be given to the `ListModel`.
- `SetModel<S, E>` represents a set-like value (of type `S`) of other models (of type `E`). Note that `S` is not required to be of type `Set<E>` but then a transformation function (`Function<S, Set<E>>`) must be given to the `SetModel`.
- `MapModel<M, K, V>` represents a map-like value (of type `M`) of keys (`K`) and values (`V`). As with lists and sets, `M` is not required to be of type `Map<K, V>` but a `Function<M, Map<K, V>>` must be given to the `MapModel`.
- `BeanModel<B>` represents a bean of type `B`. A `BeanModel` contains a map of properties (`Map<String, Property<B, ?>>`).

The `Model<T>` interface accepts `DiffVisitor<T, R>`s (`R` being the result type).

### Example

Lets define the following classes:

```java
public class Person {

	private String firstName;
	private String lastName;
	private LocalDate birthDate;
	private Address mainAddress;
	private Set<Address> formerAddresses;
	
	// getters and setters
}

public class Address {

	private String street;
	private String houseNumber;
	private String zipCode;
	private String city;
	private Country country;

	// getters and setters
}

public enum Country {

	USA, FRANCE, GERMANY, ITALY, SPAIN, JAPAN...
}
```

The structure of the class Person can be defined as follows:

```java
Model<Address> addressModel = Model.bean(Address.class,
	Model.property("street", Address::getStreet, Model.STRING),
	Model.property("houseNumber", Address::getHouseNumber, Model.STRING),
	Model.property("city", Address::getZipCode, Model.STRING),
	Model.property("country", Address::getCity, Model.STRING),
	Model.property("street", Address::getCountry, Model.value(Country.class))
);

Model<Person> personModel = Model.bean(Person.class,
	Model.property("firstName", Person::getFirstName, Model.STRING),
	Model.property("lastName", Person::getLastName, Model.STRING),
	Model.property("birthDate", Person::getBirthDate, Model.value(LocalDate.class)),
	Model.property("mainAddress", Person::getMainAddress, addressModel),
	Model.property("formerAddresses", Person::getFormerAddresses, Model.set(addressModel))
);
```

Note that instead of using getters method references (`Person::getFirstName`), a lambda could also be used (`(Person p) -> p.getFirstName().toUpperCase()`).
This allows to transform the values before they are used (if that is necessary).

With that `Model<Person>` you could do the following:

```java
Person person = ... // create some person instance

String string = personModel.toString(person); // create a generic string representation for person

int comparison = personModel.compare(person, anotherPerson); // compare person with anotherPerson by comparing fields in the order they were defined (firstName, lastName, birthDate, mainAddress.street, mainAddress.houseNumber...)

personModel.get(person, Path.of("mainAddress", "city")); // extract person.mainAddress.city

Diff<Person> diff = personModel.accept(new ComputeDiffModelVisitor<>(person, anotherPerson)); // returns a Diff<Person>, see below for more explanation.
```

## The Diff interface

The `Diff<T>` interface represents the difference between two instances of `T` (left and right).

A diff:
- has a status in (`ADDED`, `REMOVED`, `UNCHANGED`, `MODIFIED`)
- has left and right values of type `T`
- can be visited by a `DiffVisitor<T, R>`

A diff can be of one of the following concrete classes:
- `SimpleDiff<T>` representing a diff between two atomic values of type `T`
- `ListDiff<C, E>` representing a diff between two list-like values of type `C` containing elements of type `E`
- `MapDiff<M, K, V>` representing a diff between two map-like values of type `M` containing entries with key of type `K` and value of type `V`
- `BeanDiff<B>` representing a diff between two beans of type `B`

### Basic example

``` 
left = President {
	firstNames: ['George', 'Herbert', 'Walker'],
	lastName: 'Bush',
	birthYear: 1924,
	electedYears: [1989]
}

right = President {
	firstNames: ['George', 'Walker'],
	lastName: 'Bush',
	birthYear: 1946,
	electedYears: [2001, 2005]
}

diff(left, right) = BeanDiff<President>(status = MODIFIED, left = ..., right = ...) {
	'firstNames': ListDiff<List<String>, String>(status = MODIFIED, left = ..., right = ...) [
		SimpleDiff<String>(status = UNCHANGED, left = 'George', right = 'George'),
		SimpleDiff<String>(status = REMOVED, left = 'Herbert', right = null),
		SimpleDiff<String>(status = UNCHANGED, left = 'Walker', right = 'Walker'),
	],
	'lastName': SimpleDiff<String>(status = UNCHANGED, left = 'Bush', right = 'Bush'),
	'birthYear': SimpleDiff<Integer>(status = MODIFIED, left = 1924, right = 1946),
	'electedYears': ListDiff<List<Integer>, Integer>(status = MODIFIED, left = ..., right = ...) [
		SimpleDiff<Integer>(status = REMOVED, left = 1989, right = null),
		SimpleDiff<Integer>(status = ADDED, left = null, right = 2001),
		SimpleDiff<Integer>(status = ADDED, left = null, right = 2005),
	]
}
```

## Paths

A path object which can be used by both `Model` and `Diff` is also available:

```java
Path path = Path.of("previousAddresses", 2, "street");

Person left = ...
Person right = ...
Model<Person> personModel = ...
Diff<Person> personDiff = personModel.diff(left, right);

Object object = personModel.get(left, path); // extract value from left
Diff<?> diff = personDiff.get(path); // extract diff
```

## Examples

Examples can be found on the [codegeny-beans-examples repo](https://github.com/codegeny/codegeny-beans-examples).