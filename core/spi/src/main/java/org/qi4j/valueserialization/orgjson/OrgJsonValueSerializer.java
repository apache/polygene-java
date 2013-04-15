/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2010, Niclas Hehdman. All Rights Reserved.
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
package org.qi4j.valueserialization.orgjson;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.json.JSONWriter;
import org.qi4j.spi.value.ValueSerializerAdapter;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializer.OrgJsonOutput;

/**
 * ValueSerializer producing Values state as JSON documents using org.json.
 */
public class OrgJsonValueSerializer
    extends ValueSerializerAdapter<OrgJsonOutput>
{

    /**
     * Helper to pass around the Writer alongside the JSONWriter so we can flush it onSerializationEnd.
     *
     * This is needed because the org.json package do not allow to get a handle on the Writer from a JSONWriter.
     */
    public static class OrgJsonOutput
    {

        /* package */ final Writer writer;
        /* package */ final JSONWriter json;

        private OrgJsonOutput( Writer writer, JSONWriter json )
        {
            this.writer = writer;
            this.json = json;
        }
    }

    //
    // Serialization
    //
    @Override
    protected OrgJsonOutput adaptOutput( OutputStream output )
        throws Exception
    {
        Writer writer = new OutputStreamWriter( output, "UTF-8" );
        JSONWriter json = new JSONWriter( writer );
        return new OrgJsonOutput( writer, json );
    }

    @Override
    protected void onSerializationEnd( Object object, OrgJsonOutput output )
        throws Exception
    {
        output.writer.flush();
    }

    @Override
    protected void onArrayStart( OrgJsonOutput output )
        throws Exception
    {
        output.json.array();
    }

    @Override
    protected void onArrayEnd( OrgJsonOutput output )
        throws Exception
    {
        output.json.endArray();
    }

    @Override
    protected void onObjectStart( OrgJsonOutput output )
        throws Exception
    {
        output.json.object();
    }

    @Override
    protected void onObjectEnd( OrgJsonOutput output )
        throws Exception
    {
        output.json.endObject();
    }

    @Override
    protected void onFieldStart( OrgJsonOutput output, String fieldName )
        throws Exception
    {
        output.json.key( fieldName );
    }

    @Override
    protected void onValue( OrgJsonOutput output, Object value )
        throws Exception
    {
        output.json.value( value );
    }
}
