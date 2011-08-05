package org.qi4j.runtime.injection.provider;

import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public UsesInjectionProviderFactory()
    {
    }

    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        return new UsesInjectionProvider( dependencyModel );
    }

    private class UsesInjectionProvider
        implements InjectionProvider
    {
        private final DependencyModel dependency;

        public UsesInjectionProvider( DependencyModel dependency )
        {
            this.dependency = dependency;
        }

        @SuppressWarnings( "unchecked" )
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            UsesInstance uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            Object usesObject = uses.useForType( injectionType );

            if( usesObject == null && !dependency.optional())
            {
                // No @Uses object provided
                // Try instantiating a Transient or Object for the given type
                ModuleInstance moduleInstance = context.module();

                try
                {
                    usesObject = moduleInstance.newTransient( injectionType, uses.toArray() );
                } catch( NoSuchCompositeException e )
                {
                    try
                    {
                        usesObject = moduleInstance.newObject( injectionType, uses.toArray() );
                    } catch( NoSuchObjectException e1 )
                    {
                        // Could not instantiate an instance - continue anyway
                    }
                }

                if( usesObject != null )
                {
                    context.setUses( context.uses().use( usesObject ) ); // Use this for other injections in same graph
                }

                return usesObject;
            }
            else
            {
                return usesObject;
            }
        }
    }
}