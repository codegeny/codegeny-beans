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
package org.codegeny.beans.path;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.lang.reflect.Type;

public final class JsonConverter implements Converter<String> {

    private final Jsonb jsonb;

    public JsonConverter(JsonbConfig config) {
        jsonb = JsonbBuilder.create(config);
    }

    public JsonConverter() {
        this(new JsonbConfig());
    }

    @Override
    public <T> T convert(Type type, String source) {
        return jsonb.fromJson(source, type);
    }
}
