package org.qi4j.manual.recipes.concern;

import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.GenericConcern;

// START SNIPPET: appliesTo
@AppliesTo( { MyAnnotation.class, MyAppliesToFilter.class } )
// START SNIPPET: class
public class MyGenericConcern extends GenericConcern
{
// END SNIPPET: appliesTo
    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        // Do whatever you want

// START SNIPPET: class
// END SNIPPET: class
        return next.invoke( proxy, method, args );
    }
}
// END SNIPPET: class
