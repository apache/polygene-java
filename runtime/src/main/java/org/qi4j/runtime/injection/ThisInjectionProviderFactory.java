package org.qi4j.runtime.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.runtime.composite.qi.BindingContext;
import org.qi4j.runtime.composite.qi.DependencyModel;
import org.qi4j.runtime.composite.qi.InjectionContext;
import org.qi4j.runtime.composite.qi.InjectionProvider;
import org.qi4j.runtime.composite.qi.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public final class ThisInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        if( bindingContext.composite() != null )
        {
            // If Composite type then return real type, otherwise use the specified one
            Class thisType = dependencyModel.rawInjectionType();

            if( thisType.isAssignableFrom( bindingContext.composite().type() ) )
            {
                thisType = bindingContext.composite().type();
            }

            return new ThisInjectionProvider( thisType );

/* TODO Needs to be fixed to support internal mixins
            // Check if the composite implements the desired type
            if( resolution.getRawType().isAssignableFrom( fragmentKey.getCompositeType() ) )
            {
                return new ThisInjectionProvider(resolution.getRawType());
            }
            else
            {
                throw new InvalidInjectionException( "Composite " + fragmentKey.getCompositeType() + " does not implement @This type " + resolution.getDependencyType() + " in fragment " + resolution.getDependentType() );
            }
*/
        }
        else
        {
            throw new InvalidInjectionException( "Object " + dependencyModel.injectedClass() + " may not use @This" );
        }
    }

    private class ThisInjectionProvider implements InjectionProvider
    {
        Constructor proxyConstructor;

        public ThisInjectionProvider( Class type )
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
                InvocationHandler handler = context.compositeInstance();
                Object proxy = proxyConstructor.newInstance( handler );
                return proxy;
            }
            catch( Exception e )
            {
                throw new InjectionProviderException( "Could not instantiate @This proxy", e );
            }
        }
    }
}
