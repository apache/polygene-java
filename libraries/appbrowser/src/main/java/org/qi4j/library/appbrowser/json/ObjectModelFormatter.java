package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.object.ObjectDescriptor;

public class ObjectModelFormatter extends AbstractJsonFormatter<ObjectDescriptor, Void>
{
    public ObjectModelFormatter( JSONWriter writer )
    {
        super(writer);
    }

    @Override
    public void enter( ObjectDescriptor visited )
        throws JSONException
    {
        object();
        field( "visibility", visited.visibility().toString());
    }

    @Override
    public void leave( ObjectDescriptor visited )
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
