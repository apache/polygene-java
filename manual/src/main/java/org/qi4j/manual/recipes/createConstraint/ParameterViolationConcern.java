package org.qi4j.manual.recipes.createConstraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;

// START SNIPPET: report
public class ParameterViolationConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        try
        {
            return next.invoke( proxy, method, args );
        }
        catch( ConstraintViolationException e )
        {
            for( ConstraintViolation violation : e.constraintViolations() )
            {
                String name = violation.name();
                Object value = violation.value();
                Annotation constraint = violation.constraint();
                report( name, value, constraint );
            }
            throw new IllegalArgumentException("Invalid argument(s)", e);
        }
    }

// END SNIPPET: report
// START SNIPPET: report
    private void report( String name, Object value, Annotation constraint )
    {
    }
}
// END SNIPPET: report
