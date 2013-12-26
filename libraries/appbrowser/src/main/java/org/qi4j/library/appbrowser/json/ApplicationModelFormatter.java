package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.library.appbrowser.Formatter;

public class ApplicationModelFormatter extends AbstractJsonFormatter<ApplicationDescriptor, Void>
{

    public ApplicationModelFormatter( JSONWriter writer )
    {
        super( writer );
    }

    @Override
    public void enter( ApplicationDescriptor visited )
        throws JSONException
    {
        object();
        field( "name", visited.name() );
        array("layers");
    }

    @Override
    public void leave( ApplicationDescriptor visited )
        throws JSONException
    {
        endArray();
        endObject();
    }

    @Override
    public void visit( Void visited )
        throws JSONException
    {
    }
}
