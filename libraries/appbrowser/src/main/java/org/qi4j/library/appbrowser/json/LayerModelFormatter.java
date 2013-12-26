package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.UsedLayersDescriptor;

public class LayerModelFormatter extends AbstractJsonFormatter<LayerDescriptor, Void>
{
    public LayerModelFormatter( JSONWriter writer )
    {
        super( writer );
    }

    @Override
    public void enter( LayerDescriptor visited )
        throws JSONException
    {
        object();
        field( "name", visited.name() );
        array( "uses" );
        UsedLayersDescriptor usedLayersDescriptor = visited.usedLayers();
        for( LayerDescriptor used : usedLayersDescriptor.layers() )
        {
            value( used.name() );
        }
        endArray();
        array( "modules" );
    }

    @Override
    public void leave( LayerDescriptor visited )
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
