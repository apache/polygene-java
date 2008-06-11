package org.qi4j.runtime.injection.provider;

import org.qi4j.runtime.composite.qi.Resolution;
import org.qi4j.runtime.composite.qi.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;

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
            UsesInstance uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            return uses.useForType( injectionType );
        }
    }
}