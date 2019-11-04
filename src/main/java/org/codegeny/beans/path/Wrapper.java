package org.codegeny.beans.path;

public interface Wrapper<S> {

    Object get(S... elements);

    void set(S value, S... elements);
}
