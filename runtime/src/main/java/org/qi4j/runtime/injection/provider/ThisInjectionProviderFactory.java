package org.qi4j.runtime.injection.provider;

import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * JAVADOC
 */
public final class ThisInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( Resolution bindingContext, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        if( bindingContext.model() instanceof CompositeDescriptor )
        {
            // If Composite type then return real type, otherwise use the specified one
            Class thisType = dependencyModel.rawInjectionType();

            if( thisType.isAssignableFrom( bindingContext.model().type() ) )
            {
                thisType = bindingContext.model().type();
            }
            else
            {
                CompositeDescriptor acd = ( (CompositeDescriptor) bindingContext.model() );
                boolean ok = false;
                for( Class mixinType : acd.mixinTypes() )
                {
                    if( thisType.isAssignableFrom( mixinType ) )
                    {
                        ok = true;
                        break;
                    }
                }

                if( !ok )
                {
                    throw new InvalidInjectionException( "Composite " + bindingContext.model()
                        .type()
                        .getName() + " does not implement @This type " + thisType.getName() + " in fragment " + dependencyModel
                        .injectedClass()
                        .getName() );
                }
            }

            return new ThisInjectionProvider( thisType );
        }
        else
        {
            throw new InvalidInjectionException( "Object " + dependencyModel.injectedClass() + " may not use @This" );
        }
    }

    private class ThisInjectionProvider
        implements InjectionProvider
    {
        Constructor proxyConstructor;

        public ThisInjectionProvider( Class type )
        {
            try
            {
                Class proxyClass = Proxy.class.isAssignableFrom( type ) ? type : Proxy.getProxyClass( type.getClassLoader(), new Class[]{ type } );

                proxyConstructor = proxyClass.getConstructor( InvocationHandler.class );
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
                if( handler == null )
                {
                    handler = context.proxyHandler();
                }
                return proxyConstructor.newInstance( handler );
            }
            catch( Exception e )
            {
                throw new InjectionProviderException( "Could not instantiate @This proxy", e );
            }
        }
    }
}
