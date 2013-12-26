package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.mixin.MixinDescriptor;

public class MixinModelFormatter extends AbstractJsonFormatter<MixinDescriptor, Void>
{
    public MixinModelFormatter( JSONWriter writer )
    {
        super( writer );
    }

    @Override
    public void enter( MixinDescriptor visited )
        throws JSONException
    {
        object();
        field( "mixin", visited.mixinClass().getName() );
    }

    @Override
    public void leave( MixinDescriptor visited )
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
