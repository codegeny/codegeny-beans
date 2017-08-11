![Status experimental](https://img.shields.io/badge/status-experimental-red.svg)
[![Build Status](https://img.shields.io/travis/codegeny/codegeny-beans.svg)](https://travis-ci.org/codegeny/codegeny-beans)
[![Code Coverage](https://img.shields.io/codecov/c/github/codegeny/codegeny-beans.svg)](https://codecov.io/gh/codegeny/codegeny-beans)
[![Code Analysis](https://img.shields.io/codacy/grade/2a447b2e20e34b628cef941f7619e184.svg)](https://www.codacy.com/app/codegeny/codegeny-beans)

# codegeny-beans

Express your bean structure in a reflection-free and type-safe model; get generic diff/comparison/toString/toJson...

This project makes heavy (maybe too much) use of the visitor pattern to avoid casting where possible.

## The Model interface

The `Model<T>` interface represent an object of type `T` and can be of one of the following concrete classes:

- `ValueModel<T>` which represents an atomic value of type `T`. Atomic values must be comparable or a comparator must be given to the `ValueModel`.
- `ListModel<C, E>` represents a list-like value (of type `C`) of other models (of type `E`). Node that `C` is not required to be of type `List<E>` but then a transformation function (`Function<C, List<E>>`) must be given.
- `SetModel<C, E>` represents a set-like value (of type `C`) of other models (of type `E`). Node that `C` is not required to be of type `Set<E>` but then a transformation function (`Function<C, Set<E>>`) must be given.
- `MapModel<M, K, V>` represents a map-like value (of type `M`) of keys (`K`) and values (`V`). As with lists and sets, `M` is not required to be of type `Map<K, V>` but a `Function<M, Map<K, V>>` must be given.
- `BeanModel<B>` represents a bean of type `B`. A `BeanModel` contains a map of properties (`Map<String, Property<B, ?>>`).

The `Model<T>` interface is just an interface which accepts a `DiffVisitor<T, R>` (`R` being the result).

Example:

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

The structure of the class Person can be defined as follows;

```java
Model<Address> addressModel = Model.bean()
	.addProperty("street", Address::getStreet, Model.value())
	.addProperty("houseNumber", Address::getHouseNumber, Model.value())
	.addProperty("city", Address::getZipCode, Model.value())
	.addProperty("country", Address::getCity, Model.value())
	.addProperty("street", Address::getCountry, Model.value());

Model<Person> personModel = Model.bean()
	.addProperty("firstName", Person::getFirstName, Model.value())
	.addProperty("lastName", Person::getLastName, Model.value())
	.addProperty("birthDate", Person::getBirthDate, Model.value())
	.addProperty("mainAddress", Person::getMainAddress, addressModel)
	.addProperty("formerAddresses", Person::getFormerAddresses, Model.set(addressModel));
```

With that `Model<Person>` you could do the following:

```java
Person person = ... // create some person instance

personModel.toString(person); // create a generic string representation for person

personModel.compareTo(anotherPerson); // compare person with anotherPerson by comparing fields in the order they were defined (firstName, lastName, birthDate, mainAddress.street, mainAddress.houseNumber...

personModel.extract(person, Path.path().property("mainAddress").property("city")); // extract person.mainAddress.city

Diff<Person> diff = personModel.diff(anotherPerson, 0.8); // returns a Diff<Person>, see further in the documentation.
```

## The Diff interface

The `Diff<T>` interface represents the difference between two instances of `T` (left and right).

A diff:
- has a score in [0; 1]
- has a status in (`ADDED`, `REMOVED`, `UNCHANGED`, `MODIFIED`)
- has left and right values of type `T`
- can be visited by a `DiffVisitor<T, R>`

A diff can be of one of the following concrete classes:
- `SimpleDiff<T>` representing a diff between two atomic values of type `T`, its score is always 0 or 1
- `ListDiff<C, E>` representing a diff between two list-like values of type `C` containing elements of type `E`, its score being the average score of its elements
- `MapDiff<M, K, V>` representing a diff between two map-like values of type `M` containing entries with key of type `K` and value of type `V`, its score being the average score of its entries
- `BeanDiff<B>` representing a diff between two beans of type `B`, its score being the average score of its properties

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

diff(left, right) = BeanDiff<President>(score = 5/12, status = MODIFIED, left = ..., right = ...) {
	firstNames: ListDiff<List<String>, String>(score = 2/3, status = MODIFIED, left = ..., right = ...) [
		SimpleDiff<String>(score = 1, status = UNCHANGED, left = 'George', right = 'George'),
		SimpleDiff<String>(score = 0, status = REMOVED, left = 'Herbert', right = null),
		SimpleDiff<String>(score = 1, status = UNCHANGED, left = 'Walker', right = 'Walker'),
	],
	lastName: SimpleDiff<String>(score = 1, status = UNCHANGED, left = 'Bush', right = 'Bush'),
	birthYear: SimpleDiff<Integer>(score = 0, status = MODIFIED, left = 1924, right = 1946),
	electedYears: ListDiff<List<Integer>, Integer>(score = 0/3, status = MODIFIED, left = ..., right = ...) [
		SimpleDiff<Integer>(score = 0, status = REMOVED, left = 1989, right = null),
		SimpleDiff<Integer>(score = 0, status = ADDED, left = null, right = 2001),
		SimpleDiff<Integer>(score = 0, status = ADDED, left = null, right = 2005),
	]
}
```

### Comparison strategies

When comparing 2 collections (2 sets for example), at some point, the comparison algorithms will build a matrix like this:

|       |left:0|left:1|left:2|
|right:0|   90%|   80%|    0%|
|right:1|   80%|   20%|   30%|

which represents the diff score between each element in the left collection and each element in the right collection.

There are currently two available strategies for comparing collections:
- Global score maximizer which will try to find the best permutation to achieve the greater score
- Local score maximizer which will always take the greatest score in the matrix without maximizing the global result

Global score will pair the result like this: (L0, R1), (L1, R0), (L2, null) = 80% + 80% + 0% giving an average of ~53.33%
Local score will pair the result like this: (L0, R0), (L1, null), (L2, R1) = 90% + 0% + 30% giving an average of 40%

The big difference here is the speed of execution:
- Local score will find the best way to pair elements in O(m x n) (where m and n are the sizes of the left and right collections)
- Global score will find the best way to pair elements in factorial times which could take very very long for big collections (especially if all elements are almost identical).

For this reason, it is better to use the local strategy and only use global strategy if your object graph is very limited.

## Paths

A path API which is used by both `Model` and `Diff` is also available:

```java
Path path = Path.path().property("previousAddresses").index(2).property("street");

Person left = ...
Person right = ...
Model<Person> personModel = ...
Diff<Person> personDiff = personModel.diff(left, right);

Object object = personModel.extract(left, path); // extract value from left
Diff<?> diff = personDiff.extract(path); // extract diff
```