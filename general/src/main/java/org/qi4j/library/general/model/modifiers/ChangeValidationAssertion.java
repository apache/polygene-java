package org.qi4j.library.general.model.modifiers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.annotation.scope.AssertionFor;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.library.general.model.Validatable;

/**
 * After invocation, ensure that the validation rules pass.
 */
public class ChangeValidationAssertion
    implements InvocationHandler
{
    @ThisAs Validatable validatable;
    @AssertionFor InvocationHandler next;

    public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
    {
        try
        {
            return next.invoke( object, method, objects );
        }
        finally
        {
            // Ensure that object is still in a valid state
            validatable.checkValid();
        }
    }
}
