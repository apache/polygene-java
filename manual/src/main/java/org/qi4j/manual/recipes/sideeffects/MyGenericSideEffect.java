package org.qi4j.manual.recipes.sideeffects;

import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.sideeffect.GenericSideEffect;

// START SNIPPET: appliesTo
@AppliesTo( { MyAnnotation.class, MyAppliesToFilter.class } )
// START SNIPPET: body
public class MyGenericSideEffect extends GenericSideEffect
{
// END SNIPPET: appliesTo
    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        // Do whatever you need...

        try
        {
            // It is possible to obtain the returned values by using 'result' member;
            Object returnedValue = result.invoke( proxy, method, args );
        } catch( NumberFormatException e )
        {
            // And Exception will be thrown accordingly, in case you need to know.
            throw new IllegalArgumentException(); // But any thrown exceptions are ignored.
        }
        return 23; // Return values will also be ignored.
    }
// START SNIPPET: appliesTo
}
// END SNIPPET: appliesTo
// END SNIPPET: body
