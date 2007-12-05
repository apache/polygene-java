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
public class DecorateInjectionProviderFactory
    implements InjectionProviderFactory
{
    public DecorateInjectionProviderFactory()
    {
    }

    public InjectionProvider newInjectionProvider( InjectionResolution resolution ) throws InvalidInjectionException
    {
        return new DecorateInjectionProvider( resolution );
    }

    private class DecorateInjectionProvider implements InjectionProvider
    {
        private InjectionResolution resolution;

        public DecorateInjectionProvider( InjectionResolution resolution )
        {
            this.resolution = resolution;
        }

        public Object provideInjection( InjectionContext context )
        {
            Object decoratedObject;
            if( context instanceof ObjectInjectionContext )
            {
                ObjectInjectionContext objectInjectionContext = (ObjectInjectionContext) context;
                decoratedObject = objectInjectionContext.getDecorated();
            }
            else if( context instanceof MixinInjectionContext )
            {
                MixinInjectionContext mixinInjectionContext = (MixinInjectionContext) context;
                decoratedObject = mixinInjectionContext.getDecorated();
            }
            else
            {
                return null;
            }

            if( resolution.getInjectionModel().getInjectionClass().isAssignableFrom( decoratedObject.getClass() ) )
            {
                return decoratedObject;
            }

            return null;
        }
    }
}