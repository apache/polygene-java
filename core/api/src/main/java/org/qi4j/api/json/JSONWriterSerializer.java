package org.qi4j.api.json;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.io.Writer;

/**
 * Serialize values to JSON using json.org
 */
public class JSONWriterSerializer
    extends JSONSerializer
{
    JSONWriter json;

    public JSONWriterSerializer()
    {
        this.json = new JSONStringer();
    }

    public JSONWriterSerializer( Writer writer )
    {
        this.json = new JSONWriter( writer );
    }

    public JSONWriterSerializer( JSONWriter writer )
    {
        this.json = writer;
    }

    @Override
    public JSONSerializer key( String value ) throws JSONException
    {
        json.key( value );
        return this;
    }

    @Override
    public JSONSerializer value( Object value ) throws JSONException
    {
        json.value( value );
        return this;
    }

    @Override
    public JSONSerializer objectStart() throws JSONException
    {
        json.object();
        return this;
    }

    @Override
    public JSONSerializer objectEnd() throws JSONException
    {
        json.endObject();
        return this;
    }

    @Override
    public JSONSerializer arrayStart() throws JSONException
    {
        json.array();
        return this;
    }

    @Override
    public JSONSerializer arrayEnd() throws JSONException
    {
        json.endArray();
        return this;
    }

    public JSONWriter getJSON()
    {
        return json;
    }
}
