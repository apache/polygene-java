package org.qi4j.runtime.injection.provider;

import org.qi4j.runtime.composite.CompositeBuilderInstance;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.object.ObjectBuilderInstance;
import org.qi4j.runtime.structure.ModuleInstance;

import java.io.Serializable;

/**
 * JAVADOC
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
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
        implements InjectionProvider, Serializable
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

            if( usesObject == null && !dependency.optional() )
            {
                // No @Uses object provided
                // Try instantiating a Composite or Object for the given type
                ModuleInstance moduleInstance = context.moduleInstance();

                ModuleInstance.CompositeFinder compositeFinder = moduleInstance.findCompositeModel( dependency.injectionClass() );
                if( compositeFinder.model != null )
                {
                    if( Iterable.class.equals( injectionType ) )
                    {
                        usesObject = new CompositeBuilderInstance( compositeFinder.module, compositeFinder.model, uses );
                    }
                    else
                    {
                        usesObject = compositeFinder.model.newCompositeInstance( compositeFinder.module, uses, context.state() );
                    }
                }
                else
                {
                    ModuleInstance.ObjectFinder objectFinder = moduleInstance.findObjectModel( dependency.injectionClass() );
                    if( objectFinder.model != null )
                    {
                        if( Iterable.class.equals( injectionType ) )
                        {
                            usesObject = new ObjectBuilderInstance( objectFinder.module, objectFinder.model, uses );
                        }
                        else
                        {
                            usesObject = objectFinder.model.newInstance( objectFinder.module, uses );
                        }
                    }
                }

                if( usesObject != null )
                {
                    uses.use( usesObject ); // Use this for other injections in same graph
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