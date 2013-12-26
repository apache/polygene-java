package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.library.appbrowser.Formatter;

public class ValueModelFormatter extends AbstractJsonFormatter<ValueDescriptor,Void>
{
    public ValueModelFormatter( JSONWriter writer )
    {
        super(writer);
    }

    @Override
    public void enter( ValueDescriptor visited )
        throws JSONException
    {
        object();
        field( "type", visited.valueType().mainType().getName() );
        field( "visibility", visited.visibility().toString() );
    }

    @Override
    public void leave( ValueDescriptor visited )
        throws JSONException
    {
        endObject();
    }

    @Override
    public void visit( Void visited )
        throws JSONException
    {

    }
}
