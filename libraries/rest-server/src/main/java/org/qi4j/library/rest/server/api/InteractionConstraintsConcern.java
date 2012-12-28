package org.qi4j.library.rest.server.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.constraint.InteractionConstraintDeclaration;
import org.qi4j.library.rest.server.api.constraint.RequiresValid;
import org.qi4j.library.rest.server.restlet.InteractionConstraints;

/**
 * Add this concern to all interaction methods that use constraints
 */
@AppliesTo( InteractionConstraintsConcern.HasInteractionConstraints.class )
public class InteractionConstraintsConcern
    extends GenericConcern
{
    @Service
    private InteractionConstraints interactionConstraints;

    @Service
    private Module module;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( !interactionConstraints.isValid( proxy.getClass(), ObjectSelection.current(), module ) )
        {
            throw new IllegalStateException( "Not allowed to invoke interaction " + method.getName() );
        }

        if( !interactionConstraints.isValid( method, ObjectSelection.current(), module ) )
        {
            throw new IllegalStateException( "Not allowed to invoke interaction " + method.getName() );
        }

        return next.invoke( proxy, method, args );
    }

    public static class HasInteractionConstraints
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            for( Annotation annotation : method.getAnnotations() )
            {
                if( annotation.annotationType().equals( RequiresValid.class ) ||
                    annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null ||
                    annotation.annotationType().getAnnotation( InteractionConstraintDeclaration.class ) != null )
                {
                    return true;
                }
            }
            return false;
        }
    }
}
