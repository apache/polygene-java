package org.qi4j.runtime.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.qi4j.InvocationContext;
import org.qi4j.dependency.DependencyInjectionContext;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.dependency.ModifierDependencyInjectionContext;
import org.qi4j.model.DependencyKey;

/**
 * TODO
 */
public class InvocationDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        if( key.getDependencyType().equals( Method.class ) ||
            key.getDependencyType().equals( AnnotatedElement.class ) ||
            key.getDependencyType().equals( InvocationContext.class ) ||
            Annotation.class.isAssignableFrom( key.getDependencyType() ) )
        {
            return new InvocationDependencyResolution( key );
        }
        else
        {
            throw new InvalidDependencyException( "Invalid dependency type " + key.getDependencyType() + " in " + key.getDependentType() );
        }
    }

    private class InvocationDependencyResolution implements DependencyResolution
    {
        private DependencyKey key;

        public InvocationDependencyResolution( DependencyKey key )
        {
            this.key = key;
        }

        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            ModifierDependencyInjectionContext modifierContext = (ModifierDependencyInjectionContext) context;

            if( key.getDependencyType().equals( Method.class ) )
            {
                // This needs to be updated to handle Apply and annotation aggregation correctly
                return modifierContext.getMethod();
            }
            else if( key.getDependencyType().equals( AnnotatedElement.class ) )
            {
                return modifierContext.getMethod();
            }
            else if( key.getDependencyType().equals( InvocationContext.class ) )
            {
                return modifierContext.getInvocationContext();
            }
            else
            {
                return modifierContext.getModel().getAnnotation( key.getDependencyType(), modifierContext.getMethod() );
            }
        }
    }
}