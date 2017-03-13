/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.spi.serialization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.type.ValueType;

/**
 * Serialization Settings.
 *
 * Serialization implementations might extend this with additional specialized settings.
 *
 * @param <SettingsType> Specialization type of SerializationSettings for a fluent usage
 */
public class SerializationSettings<SettingsType extends SerializationSettings>
{
    public static final SerializationSettings DEFAULT = new SerializationSettings();

    public static SerializationSettings orDefault( SerializationSettings settings )
    {
        return settings != null ? settings : DEFAULT;
    }

    private final Map<ValueType, Converter<Object>> converters = new LinkedHashMap<>();

    public final Map<ValueType, Converter<Object>> getConverters()
    {
        return Collections.unmodifiableMap( converters );
    }

    @SuppressWarnings( "unchecked" )
    public final SettingsType withConverter( ValueType valueType, Converter<Object> adapter )
    {
        converters.put( valueType, adapter );
        return (SettingsType) this;
    }

    public final SettingsType withConverter( Converter<Object> adapter )
    {
        return withConverter( ValueType.of( adapter.type() ), adapter );
    }
}
