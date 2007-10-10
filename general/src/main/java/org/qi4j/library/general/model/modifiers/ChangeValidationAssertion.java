package org.qi4j.library.general.model.modifiers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.AppliesToFilter;
import org.qi4j.api.annotation.scope.AssertionFor;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.library.general.model.Validatable;

/**
 * After invocation, ensure that the validation rules pass.
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

        public boolean appliesTo( Method method, Class mixin, Class compositeType )
        {
            if( !method.getReturnType().equals( Void.TYPE ) )
            {
                return false;
            }

            return !this.checkValidMethod.equals( method );
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
            if( method.getReturnType().equals( Void.TYPE ) )
            {
                // Ensure that object is still in a valid state
                validatable.checkValid();
            }
        }
    }
}
