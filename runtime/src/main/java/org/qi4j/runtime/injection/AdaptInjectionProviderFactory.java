package org.qi4j.runtime.injection;

import org.qi4j.spi.dependency.InjectionContext;
import org.qi4j.spi.dependency.InjectionProvider;
import org.qi4j.spi.dependency.InjectionProviderFactory;
import org.qi4j.spi.dependency.InjectionResolution;
import org.qi4j.spi.dependency.InvalidInjectionException;
import org.qi4j.spi.dependency.MixinInjectionContext;
import org.qi4j.spi.dependency.ObjectInjectionContext;

/**
 * TODO
 */
public class AdaptInjectionProviderFactory
    implements InjectionProviderFactory
{
    public AdaptInjectionProviderFactory()
    {
    }

    public InjectionProvider newInjectionProvider( InjectionResolution resolution ) throws InvalidInjectionException
    {
        return new AdaptInjectionProvider( resolution );
    }

    private class AdaptInjectionProvider implements InjectionProvider
    {
        private InjectionResolution resolution;

        public AdaptInjectionProvider( InjectionResolution resolution )
        {
            this.resolution = resolution;
        }

        public Object provideInjection( InjectionContext context )
        {
            Iterable<Object> adapt;
            if( context instanceof ObjectInjectionContext )
            {
                ObjectInjectionContext objectInjectionContext = (ObjectInjectionContext) context;
                adapt = objectInjectionContext.getAdapt();
            }
            else if( context instanceof MixinInjectionContext )
            {
                MixinInjectionContext mixinInjectionContext = (MixinInjectionContext) context;
                adapt = mixinInjectionContext.getAdapt();
            }
            else
            {
                return null;
            }

            for( Object adaptedObject : adapt )
            {
                if( adaptedObject.getClass().isAssignableFrom( resolution.getInjectionModel().getInjectionClass() ) )
                {
                    return adaptedObject;
                }
            }

            return null;
        }
    }
}