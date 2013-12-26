package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.library.appbrowser.Formatter;

public class ModuleModelFormatter extends AbstractJsonFormatter<ModuleDescriptor, Void>
{

    public ModuleModelFormatter( JSONWriter writer )
    {
        super( writer );
    }

    @Override
    public void enter( ModuleDescriptor visited )
        throws JSONException
    {
        object();
        field( "name", visited.name() );
    }

    @Override
    public void leave( ModuleDescriptor visited )
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
