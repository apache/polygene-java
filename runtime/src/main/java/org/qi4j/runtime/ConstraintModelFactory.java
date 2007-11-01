package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.annotation.ConstraintDeclaration;
import org.qi4j.model.MethodConstraint;
import org.qi4j.model.ParameterConstraint;

/**
 * TODO
 */
public final class ConstraintModelFactory
{
    public ConstraintModelFactory()
    {
    }

    public MethodConstraint newMethodConstraint( Method constrainedMethod )
    {
        Iterable<ParameterConstraint> paramConstraints = getParameterConstraints( constrainedMethod );

        MethodConstraint methodConstraint = new MethodConstraint( constrainedMethod, paramConstraints );
        return methodConstraint;
    }

    private Iterable<ParameterConstraint> getParameterConstraints( Method constrainedMethod )
    {
        Class[] parameterTypes = constrainedMethod.getParameterTypes();
        List<ParameterConstraint> paramConstraints = new ArrayList<ParameterConstraint>();

        Annotation[][] annotations = constrainedMethod.getParameterAnnotations();
        int idx = 0;
        for( Annotation[] parameterAnnotations : annotations )
        {
            List<Annotation> constraintAnnotations = new ArrayList<Annotation>();
            for( Annotation parameterAnnotation : parameterAnnotations )
            {
                if( isConstraint( parameterAnnotation ) )
                {
                    constraintAnnotations.add( parameterAnnotation );
                }
            }
            Class parameterType = parameterTypes[ idx ];
            ParameterConstraint paramConstraint = new ParameterConstraint( parameterType, constraintAnnotations );
            paramConstraints.add( paramConstraint );
            idx++;
        }

        return paramConstraints;
    }

    private boolean isConstraint( Annotation parameterAnnotation )
    {
        return parameterAnnotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null;
    }
}
