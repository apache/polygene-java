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
public final class DecorateInjectionProviderFactory
    implements InjectionProviderFactory
{
    public DecorateInjectionProviderFactory()
    {
    }

    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
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

            if( decoratedObject == null )
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