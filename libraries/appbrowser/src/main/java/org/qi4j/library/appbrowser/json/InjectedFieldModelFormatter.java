package org.qi4j.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.composite.DependencyDescriptor;
import org.qi4j.api.composite.InjectedFieldDescriptor;
import org.qi4j.library.appbrowser.Formatter;

public class InjectedFieldModelFormatter extends AbstractJsonFormatter<InjectedFieldDescriptor, DependencyDescriptor>
{
    public InjectedFieldModelFormatter( JSONWriter writer )
    {
        super(writer);
    }

    @Override
    public void enter( InjectedFieldDescriptor visited )
        throws JSONException
    {
        object();
        field("name", visited.field().getName() );
//        field( "optional", visited.optional() );
//        field( "injectedclass", visited.injectedClass().getName() );
//        field( "injectedannotation", visited.injectionAnnotation().toString() );
//        field( "injectedtype", visited.injectionType().toString() );
//        field( "rawinjectectiontype", visited.rawInjectionType().getName() );
    }

    @Override
    public void leave( InjectedFieldDescriptor visited )
        throws JSONException
    {
        endObject();
    }

    @Override
    public void visit( DependencyDescriptor visited )
        throws JSONException
    {

    }
}
