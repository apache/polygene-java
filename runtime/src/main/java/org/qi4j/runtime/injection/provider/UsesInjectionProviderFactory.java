package org.qi4j.runtime.injection.provider;

import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.composite.NoSuchCompositeException;
import org.qi4j.composite.ConstructionException;
import org.qi4j.object.NoSuchObjectException;

/**
 * TODO
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public UsesInjectionProviderFactory()
    {
    }

    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        return new UsesInjectionProvider( dependencyModel );
    }

    private class UsesInjectionProvider implements InjectionProvider
    {
        private final DependencyModel dependency;

        public UsesInjectionProvider( DependencyModel dependency )
        {
            this.dependency = dependency;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            UsesInstance uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            Object usesObject = uses.useForType( injectionType );

            if (usesObject == null)
            {
                // No @Uses object provided
                // Try instantiating a Composite or Object for the given type
                try
                {
                    return context.moduleInstance().compositeBuilderFactory().newComposite( injectionType );
                }
                catch( NoSuchCompositeException e )
                {
                    try
                    {
                        return context.moduleInstance().objectBuilderFactory().newObject( injectionType );
                    }
                    catch( NoSuchObjectException e1 )
                    {
                        return null;
                    }
                }
            } else
                return usesObject;
        }
    }
}