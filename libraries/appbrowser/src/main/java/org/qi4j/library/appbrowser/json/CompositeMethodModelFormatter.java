package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.composite.MethodDescriptor;
import org.qi4j.library.appbrowser.Formatter;

public class CompositeMethodModelFormatter extends AbstractJsonFormatter<MethodDescriptor, Void>
{
    public CompositeMethodModelFormatter( JSONWriter writer )
    {
        super(writer);
    }

    @Override
    public void enter( MethodDescriptor visited )
        throws JSONException
    {
        object();
        field("method", visited.method().getName() );
    }

    @Override
    public void leave( MethodDescriptor visited )
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
