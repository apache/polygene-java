package org.qi4j.runtime.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.FragmentInjectionContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public final class ThisCompositeAsInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        if( bindingContext.getCompositeResolution() != null )
        {
            return new ThisCompositeAsInjectionProvider( resolution.getInjectionModel().getInjectionClass() );

/* TODO Needs to be fixed to support internal mixins
            // Check if the composite implements the desired type
            if( resolution.getRawType().isAssignableFrom( fragmentKey.getCompositeType() ) )
            {
                return new ThisCompositeAsInjectionProvider(resolution.getRawType());
            }
            else
            {
                throw new InvalidInjectionException( "Composite " + fragmentKey.getCompositeType() + " does not implement @ThisCompositeAs type " + resolution.getDependencyType() + " in fragment " + resolution.getDependentType() );
            }
*/
        }
        else
        {
            throw new InvalidInjectionException( "Object " + resolution.getInjectionModel().getInjectedClass() + " may not use @ThisCompositeAs" );
        }
    }

    private class ThisCompositeAsInjectionProvider implements InjectionProvider
    {
        Constructor proxyConstructor;

        public ThisCompositeAsInjectionProvider( Class type )
        {
            try
            {
                proxyConstructor = Proxy.getProxyClass( type.getClassLoader(), new Class[]{ type } ).getConstructor( InvocationHandler.class );
            }
            catch( Exception e )
            {
                // Ignore
                e.printStackTrace();
            }
        }

        public Object provideInjection( InjectionContext context )
        {
            try
            {
                FragmentInjectionContext fic = (FragmentInjectionContext) context;
                InvocationHandler handler = fic.getThisCompositeAs();
                Object proxy = proxyConstructor.newInstance( handler );
                return proxy;
            }
            catch( Exception e )
            {
                throw new InjectionProviderException( "Could not instantiate @ThisCompositeAs proxy", e );
            }
        }
    }
}
