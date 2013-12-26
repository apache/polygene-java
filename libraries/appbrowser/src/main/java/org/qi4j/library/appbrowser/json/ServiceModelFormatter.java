package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.service.ServiceDescriptor;

public class ServiceModelFormatter extends AbstractJsonFormatter<ServiceDescriptor, Void>
{
    public ServiceModelFormatter( JSONWriter writer )
    {
        super( writer );
    }

    @Override
    public void enter( ServiceDescriptor visited )
        throws JSONException
    {
        object();
        field( "identity", visited.identity() );
        field( "type", visited.primaryType().getName() );
        field( "visibility", visited.visibility().toString() );
        Class<Object> config = visited.configurationType();
        if( config != null )
        {
            field( "configuration", config.getName() );
        }
        field( "instantiateOnStartup", visited.isInstantiateOnStartup() );
    }

    @Override
    public void leave( ServiceDescriptor visited )
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
