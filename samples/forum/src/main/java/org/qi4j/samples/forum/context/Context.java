package org.qi4j.samples.forum.context;

import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

/**
 * TODO
 */
public class Context
{
    @Structure
    protected Module module;

    protected <T> T role( Class<T> roleType, Object data )
    {
        return module.newObject( roleType, data );
    }

    protected <T> T role( Object object, Class<T> roleType )
    {
        return Qi4j.FUNCTION_COMPOSITE_INSTANCE_OF.map( (Composite) object ).newProxy( roleType );
    }
}
