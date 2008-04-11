package org.qi4j.runtime.injection;

import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.MixinInjectionContext;
import org.qi4j.spi.injection.ObjectInjectionContext;

/**
 * TODO
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public UsesInjectionProviderFactory()
    {
    }

    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        return new UsesInjectionProvider( resolution );
    }

    private class UsesInjectionProvider implements InjectionProvider
    {
        private InjectionResolution resolution;

        public UsesInjectionProvider( InjectionResolution resolution )
        {
            this.resolution = resolution;
        }

        public Object provideInjection( InjectionContext context )
        {
            Iterable<Object> uses;
            if( context instanceof ObjectInjectionContext )
            {
                ObjectInjectionContext objectInjectionContext = (ObjectInjectionContext) context;
                uses = objectInjectionContext.getUses();
            }
            else if( context instanceof MixinInjectionContext )
            {
                MixinInjectionContext mixinInjectionContext = (MixinInjectionContext) context;
                uses = mixinInjectionContext.getUses();
            }
            else
            {
                return null;
            }

            for( Object usedObject : uses )
            {
                if( resolution.getInjectionModel().getRawInjectionType().isAssignableFrom( usedObject.getClass() ) )
                {
                    return usedObject;
                }
            }

            return null;
        }
    }
}