package org.codegeny.beans.model.visitor.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.function.Function;

public class ToStringJsonSerializer<T> extends JsonSerializer<T> {

    public static final JsonSerializer<Object> DEFAULT = new ToStringJsonSerializer<>(Object::toString);

    private final Function<? super T, String> printer;

    public ToStringJsonSerializer(Function<? super T, String> printer) {
        this.printer = printer;
    }

    @Override
    public void serialize(T object, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(printer.apply(object));
    }
}
