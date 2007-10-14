package org.qi4j.library.framework.validation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.AppliesToFilter;
import org.qi4j.api.annotation.scope.AssertionFor;
import org.qi4j.api.annotation.scope.ThisAs;

/**
 * After invocation, ensure that the validation rules pass.
 * <p/>
 * This applies to all methods which throws ValidationException
 */
@AppliesTo( ChangeValidationAssertion.AppliesTo.class )
public class ChangeValidationAssertion
    implements InvocationHandler
{
    public static class AppliesTo
        implements AppliesToFilter
    {
        private final Method checkValidMethod;

        public AppliesTo()
        {
            try
            {
                checkValidMethod = Validatable.class.getMethod( "checkValid" );
            }
            catch( NoSuchMethodException e )
            {
                throw new Error( "Invalid interface", e );
            }
        }

        public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modelClass )
        {
            if( method.equals( checkValidMethod ) )
            {
                return false;
            }

            Class[] exceptionClasses = method.getExceptionTypes();
            for( Class exceptionClass : exceptionClasses )
            {
                if( ValidationException.class.isAssignableFrom( exceptionClass ) )
                {
                    return true;
                }
            }

            return false;
        }
    }

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
