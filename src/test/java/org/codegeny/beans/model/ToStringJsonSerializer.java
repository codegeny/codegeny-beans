package org.codegeny.beans.model;

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
