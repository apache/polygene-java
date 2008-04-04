package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.Constraint;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.composite.Composite;

/**
 * TODO
 */
public final class ConstraintInvocationHandler
    implements InvocationHandler
{
    private ProxyReferenceInvocationHandler proxyHandler;
    private List<List<ConstraintInstance>> parameterConstraintInstances;
    private Object next;

    public ConstraintInvocationHandler( ProxyReferenceInvocationHandler proxyHandler, List<List<ConstraintInstance>> parameterConstraintInstances, Object previousConcern )
    {
        this.proxyHandler = proxyHandler;
        this.parameterConstraintInstances = parameterConstraintInstances;
        this.next = previousConcern;
    }

    public Object invoke( Object object, Method method, Object[] args )
        throws Throwable
    {
        // Check constraints
        int idx = 0;

        List<ConstraintViolation> constraintViolations = null;
        for( List<ConstraintInstance> parameterConstraintInstance : parameterConstraintInstances )
        {
            Object arg = args[ idx ];
            for( ConstraintInstance constraint : parameterConstraintInstance )
            {
                try
                {
                    Constraint constraint1 = constraint.getConstraint();
                    if( !constraint1.isValid( constraint.getAnnotation(), arg ) )
                    {
                        // Register constraint violation
                        ConstraintViolation violation = new ConstraintViolation( constraint.getAnnotation(), arg );
                        if( constraintViolations == null )
                        {
                            constraintViolations = new ArrayList<ConstraintViolation>();
                        }
                        constraintViolations.add( violation );
                    }
                }
                catch( NullPointerException e )
                {
                    // Ignore...
                    // Since we have @NotNull it is ok for Constraint implementations to throw NPE's instead of
                    // having to implement null checks
                }
            }
            idx++;
        }

        if( constraintViolations != null )
        {
            Composite composite = (Composite) proxyHandler.composite();
            throw new ConstraintViolationException( composite, method, constraintViolations );
        }

        // Invoke next
        if( next instanceof InvocationHandler )
        {
            return ( (InvocationHandler) next ).invoke( object, method, args );
        }
        else
        {
            try
            {
                return method.invoke( next, args );
            }
            catch( NullPointerException e )
            {
                e.printStackTrace();
                throw e;
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
