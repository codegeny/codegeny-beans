package org.codegeny.beans.model.visitor.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.function.Function;

public class FromStringJsonDeserializer<T> extends JsonDeserializer<T> {

    private final Function<String, ? extends T> parser;

    public FromStringJsonDeserializer(Function<String, ? extends T> parser) {
        this.parser = parser;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return parser.apply(jsonParser.readValueAs(String.class));
    }
}
