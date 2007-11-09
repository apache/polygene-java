package org.qi4j.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.ConstraintViolation;
import org.qi4j.ParameterConstraint;

/**
 * TODO
 */
public final class ConstraintInvocationHandler extends FragmentInvocationHandler
{
    private ProxyReferenceInvocationHandler proxyHandler;
    private List<List<ConstraintInstance>> parameterConstraintInstances;

    public ConstraintInvocationHandler( ProxyReferenceInvocationHandler proxyHandler, List<List<ConstraintInstance>> parameterConstraintInstances, Object previousConcern )
    {
        super( previousConcern );
        this.proxyHandler = proxyHandler;
        this.parameterConstraintInstances = parameterConstraintInstances;
    }

    public Object invoke( Object object, Method method, Object[] args ) throws Throwable
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
                    ParameterConstraint constraint1 = constraint.getConstraint();
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
                    // TODO Question from Niclas: Why??
                }
            }
            idx++;
        }

        if( constraintViolations != null )
        {
            proxyHandler.setConstraintViolations( constraintViolations );
        }

        return super.invoke( object, method, args );
    }
}
