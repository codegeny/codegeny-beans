package org.codegeny.beans.path;

import org.codegeny.beans.model.Model;

public final class ModelWrapper<T, S> implements Wrapper<S> {

    private final Converter<S> converter;
    private final Model<T> model;
    private final T instance;

    public ModelWrapper(Converter<S> converter, Model<T> model, T instance) {
        this.converter = converter;
        this.model = model;
        this.instance = instance;
    }

    @Override
    public Object get(S... elements) {
        return model.get(instance, Path.of(elements), converter);
    }

    @Override
    public void set(S value, S... elements) {
        model.set(instance, Path.of(elements), value, converter);
    }
}
