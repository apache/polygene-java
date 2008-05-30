package org.qi4j.runtime.injection;

import org.qi4j.runtime.composite.qi.DependencyModel;
import org.qi4j.runtime.composite.qi.InjectionContext;
import org.qi4j.runtime.composite.qi.InjectionProvider;
import org.qi4j.runtime.composite.qi.InjectionProviderFactory;
import org.qi4j.runtime.composite.qi.Resolution;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InvalidInjectionException;

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
        private DependencyModel dependency;

        public UsesInjectionProvider( DependencyModel dependency )
        {
            this.dependency = dependency;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            Iterable<Object> uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            for( Object usedObject : uses )
            {
                if( injectionType.isAssignableFrom( usedObject.getClass() ) )
                {
                    return usedObject;
                }
            }

            return null;
        }
    }
}