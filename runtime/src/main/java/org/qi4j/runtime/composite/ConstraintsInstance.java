package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.ConstraintViolationException;

/**
 * TODO
 */
public final class ConstraintsInstance
{
    private List<ConstraintInstance> constraintInstances;

    public ConstraintsInstance( List<ConstraintInstance> constraintInstances )
    {
        this.constraintInstances = constraintInstances;
    }

    public void checkValid( Object newValue, Method accessor )
        throws ConstraintViolationException
    {
        List<ConstraintViolation> constraintViolations = null;
        for( int i = 0; i < constraintInstances.size(); i++ )
        {
            ConstraintInstance constraintInstance = constraintInstances.get( i );
            boolean isValid;

            try
            {
                isValid = constraintInstance.getConstraint().isValid( constraintInstance.getAnnotation(), newValue );
            }
            catch( NullPointerException e )
            {
                isValid = false;
            }

            if( !isValid )
            {
                // Register constraint violation
                ConstraintViolation violation = new ConstraintViolation( constraintInstance.getAnnotation(), newValue );
                if( constraintViolations == null )
                {
                    constraintViolations = new ArrayList<ConstraintViolation>();
                }
                constraintViolations.add( violation );

            }
        }

        // New value did not pass constraints
        if( constraintViolations != null )
        {
            throw new ConstraintViolationException( accessor, constraintViolations );
        }
    }
}