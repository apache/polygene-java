package org.qi4j.runtime.injection.provider;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.spi.composite.AbstractCompositeDescriptor;
import org.qi4j.spi.util.SerializationUtil;

/**
 * JAVADOC
 */
public final class ThisInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
{
    public InjectionProvider newInjectionProvider( Resolution bindingContext, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        if( bindingContext.object() instanceof AbstractCompositeDescriptor )
        {
            // If Composite type then return real type, otherwise use the specified one
            Class thisType = dependencyModel.rawInjectionType();

            if( thisType.isAssignableFrom( bindingContext.object().type() ) )
            {
                thisType = bindingContext.object().type();
            }
            else
            {
                AbstractCompositeDescriptor acd = ( (AbstractCompositeDescriptor) bindingContext.object() );
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
                    throw new InvalidInjectionException( "Composite " + bindingContext.object()
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
        implements InjectionProvider, Serializable
    {
        Constructor proxyConstructor;

        public ThisInjectionProvider( Class type )
        {
            try
            {
                proxyConstructor = Proxy.getProxyClass( type.getClassLoader(), new Class[]{ type } )
                    .getConstructor( InvocationHandler.class );
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

        private void writeObject( ObjectOutputStream out )
            throws IOException
        {
            try
            {
                SerializationUtil.writeConstructor( out, proxyConstructor );
            }
            catch( NotSerializableException e )
            {
                System.err.println( "NotSerializable in " + getClass() );
                throw e;
            }
        }

        private void readObject( ObjectInputStream in )
            throws IOException, ClassNotFoundException
        {
            proxyConstructor = SerializationUtil.readConstructor( in );
        }
    }
}
