package org.qi4j.library.validation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.ConcernOf;
import org.qi4j.injection.scope.This;

/**
 * After invocation, ensure that the validation rules pass.
 * <p/>
 * This applies to all methods which throws ValidationException
 */
@AppliesTo( ChangeValidationConcern.AppliesTo.class )
public class ChangeValidationConcern extends ConcernOf<InvocationHandler>
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

    @This Validatable validatable;

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
