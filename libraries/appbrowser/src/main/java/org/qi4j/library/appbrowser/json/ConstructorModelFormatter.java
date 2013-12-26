package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.composite.ConstructorDescriptor;

public class ConstructorModelFormatter extends AbstractJsonFormatter<ConstructorDescriptor,Void>
{
    public ConstructorModelFormatter( JSONWriter writer )
    {
        super( writer );
    }

    @Override
    public void enter( ConstructorDescriptor visited )
        throws JSONException
    {
        object();
        field( "name", visited.constructor().getName() );
    }

    @Override
    public void leave( ConstructorDescriptor visited )
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
