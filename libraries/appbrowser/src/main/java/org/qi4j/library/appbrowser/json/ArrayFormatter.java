package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.library.appbrowser.Formatter;

public class ArrayFormatter extends AbstractJsonFormatter
    implements Formatter
{
    private final String name;

    public ArrayFormatter( JSONWriter writer, String name )
    {
        super(writer);
        this.name = name;
    }

    @Override
    public void enter( Object visited )
        throws JSONException
    {
        array(name);
    }

    @Override
    public void leave( Object visited )
        throws JSONException
    {
        endArray();
    }

    @Override
    public void visit( Object visited )
        throws JSONException
    {

    }
}
