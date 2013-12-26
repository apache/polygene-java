package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.library.appbrowser.Formatter;

public abstract class AbstractJsonFormatter<NODE,LEAF>
    implements Formatter<NODE, LEAF>
{
    private final JSONWriter writer;

    public AbstractJsonFormatter( JSONWriter writer )
    {
        this.writer = writer;
    }

    protected void field( String name, String value )
        throws JSONException
    {
        writer.key( name ).value(value);
    }

    protected void field( String name, boolean value )
        throws JSONException
    {
        writer.key( name ).value(value);
    }

    protected void array( String name )
        throws JSONException
    {
        writer.key(name);
        writer.array();
    }

    protected void endArray()
        throws JSONException
    {
        writer.endArray();
    }

    protected void object()
        throws JSONException
    {
        writer.object();
    }

    protected void object(String name)
        throws JSONException
    {
        writer.key(name);
        writer.object();
    }

    protected void endObject()
        throws JSONException
    {
        writer.endObject();
    }

    protected void value( Object value )
        throws JSONException
    {
        writer.value( value );
    }

}
