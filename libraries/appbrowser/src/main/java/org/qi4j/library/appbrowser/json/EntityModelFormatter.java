package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.entity.EntityDescriptor;

public class EntityModelFormatter extends AbstractJsonFormatter<EntityDescriptor, Void>
{

    public EntityModelFormatter( JSONWriter writer )
    {
        super( writer );
    }

    @Override
    public void enter( EntityDescriptor visited )
        throws JSONException
    {
        object();
        field( "type", visited.primaryType().getName() );
        field( "visibility", visited.visibility().toString() );
        field( "queryable", visited.queryable() );
    }

    @Override
    public void leave( EntityDescriptor visited )
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
