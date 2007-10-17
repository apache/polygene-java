package org.qi4j.library.framework.validation;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.qi4j.api.ConstraintViolation;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.AppliesToFilter;
import org.qi4j.api.annotation.scope.AssertionFor;
import org.qi4j.api.annotation.scope.Invocation;
import org.qi4j.api.annotation.scope.ThisAs;

/**
 * After invocation, ensure that the validation rules pass.
 * <p/>
 * This applies to all methods which throws ValidationException
 */
@AppliesTo( ConstraintValidationAssertion.AppliesTo.class )
public class ConstraintValidationAssertion
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

    @Invocation InvocationContext context;
    @ThisAs Validatable validatable;
    @AssertionFor InvocationHandler next;

    String name;
    String resourceBundle;

    public void setMethod( @Invocation Method method )
    {
        if( method.getName().startsWith( "set" ) )
        {
            name = Introspector.decapitalize( method.getName().substring( 3 ) );
        }
        else
        {
            name = method.getName();
        }

        resourceBundle = method.getDeclaringClass().getName();
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle( resourceBundle );
        }
        catch( MissingResourceException e )
        {
            resourceBundle = method.getDeclaringClass().getPackage() + ".package";
            try
            {
                ResourceBundle bundle = ResourceBundle.getBundle( resourceBundle );
            }
            catch( MissingResourceException e1 )
            {
                // No bundle
                resourceBundle = null;
            }
        }
    }

    public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
    {
        if( context.getConstraintViolations().size() > 0 )
        {
            // Convert constraint violations into validation messages
            Collection<ConstraintViolation> violations = context.getConstraintViolations();
            List<ValidationMessage> messages = new ArrayList<ValidationMessage>( violations.size() );
            for( ConstraintViolation violation : violations )
            {
                Annotation constraint = violation.getConstraint();
                Method[] constraintMethods = constraint.annotationType().getDeclaredMethods();
                Object[] paramValues = new Object[constraintMethods.length + 1];
                paramValues[ 0 ] = violation.getValue();
                for( int j = 0; j < constraintMethods.length; j++ )
                {
                    Object paramValue = constraintMethods[ j ].invoke( constraint );
                    paramValues[ j + 1 ] = paramValue;
                }


                ValidationMessage message = newMessage( constraint, paramValues );
                messages.add( message );
            }
            throw new ValidationException( messages );
        }


        return next.invoke( object, method, objects );
    }

    private ValidationMessage newMessage( Annotation constraint, Object[] params )
    {
        return new ValidationMessage( name + getConstraintResourceKey( constraint.annotationType() ), resourceBundle, ValidationMessage.Severity.ERROR, params );
    }

    private String getConstraintResourceKey( Class annotationType )
    {
        String name = annotationType.getSimpleName();
        StringBuffer messageBuf = new StringBuffer();
        for( int i = 0; i < name.length(); i++ )
        {
            char ch = name.charAt( i );
            if( Character.isUpperCase( ch ) )
            {
                messageBuf.append( '.' );
                messageBuf.append( Character.toLowerCase( ch ) );
            }
            else
            {
                messageBuf.append( ch );
            }
        }
        return messageBuf.toString();
    }
}
