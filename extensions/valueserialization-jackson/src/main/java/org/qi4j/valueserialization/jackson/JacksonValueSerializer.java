/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.valueserialization.jackson;

import java.io.OutputStream;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.qi4j.spi.value.ValueSerializerAdapter;

/**
 * ValueSerializer producing Values state as JSON documents using Jackson.
 */
public class JacksonValueSerializer
    extends ValueSerializerAdapter<JsonGenerator>
{

    private final JsonFactory jsonFactory = new MappingJsonFactory();

    @Override
    protected JsonGenerator adaptOutput( OutputStream output )
        throws Exception
    {
        return jsonFactory.createJsonGenerator( output );
    }

    @Override
    protected void onSerializationEnd( Object object, JsonGenerator output )
        throws Exception
    {
        output.close();
    }

    @Override
    protected void onArrayStart( JsonGenerator output )
        throws Exception
    {
        output.writeStartArray();
    }

    @Override
    protected void onArrayEnd( JsonGenerator output )
        throws Exception
    {
        output.writeEndArray();
    }

    @Override
    protected void onObjectStart( JsonGenerator output )
        throws Exception
    {
        output.writeStartObject();
    }

    @Override
    protected void onObjectEnd( JsonGenerator output )
        throws Exception
    {
        output.writeEndObject();
    }

    @Override
    protected void onFieldStart( JsonGenerator output, String fieldName )
        throws Exception
    {
        output.writeFieldName( fieldName );
    }

    @Override
    protected void onValue( JsonGenerator output, Object value )
        throws Exception
    {
        output.writeObject( value );
    }
}
