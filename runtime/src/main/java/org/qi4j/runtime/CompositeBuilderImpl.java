/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.MixinDependencyInjectionContext;
import org.qi4j.api.model.MixinResolution;
import org.qi4j.api.model.CompositeResolution;
import org.qi4j.api.persistence.Lifecycle;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    private static final Method CREATE_METHOD;

    private Class<T> compositeInterface;
    private CompositeContextImpl context;
    private FragmentFactory fragmentFactory;

    private List adaptContext;
    private List decorateContext;
    private Map<MixinResolution, Object[]> properties;

    static
    {
        try
        {
            CREATE_METHOD = Lifecycle.class.getMethod( "create" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Lifecycle class is corrupt." );
        }
    }

    CompositeBuilderImpl( CompositeContextImpl<T> context, FragmentFactory fragmentFactory )
    {
        this.fragmentFactory = fragmentFactory;
        this.context = context;
        this.compositeInterface = context.getCompositeModel().getCompositeClass();

        properties = new HashMap<MixinResolution, Object[]>();
    }

    public void adapt( Object adaptedObject )
    {
        getAdaptContext().add( adaptedObject );
    }

    public <K, T extends K> void decorate( K decoratedObject )
    {
        getDecorateContext().add( decoratedObject );
    }

    public <K> void properties( Class<K> mixinType, Object... properties )
    {
        CompositeResolution compositeResolution = context.getCompositeResolution();
        MixinResolution mixin = compositeResolution.getMixinForInterface( mixinType );
        this.properties.put( mixin, properties );
    }

    public T newInstance()
    {
        // Instantiate composite proxy
        T composite = newInstance( compositeInterface );

        // Instantiate all mixins
        CompositeInvocationHandler state = newMixins( composite );

        // Invoke lifecycle create() method
//        invokeCreate( composite, state );

        // Return
        return composite;
    }

    private <T extends Composite> T newInstance( Class<T> compositeType )
        throws CompositeInstantiationException
    {
        // Instantiate proxy for given composite interface
        try
        {
            AbstractCompositeInvocationHandler handler = new CompositeInvocationHandler( context );
            ClassLoader proxyClassloader = compositeType.getClassLoader();
            Class[] interfaces = new Class[]{ compositeType };
            return compositeType.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( e );
        }
    }


    private CompositeInvocationHandler newMixins( T composite )
    {
        CompositeInvocationHandler state = CompositeInvocationHandler.getInvocationHandler( composite );
        Map states = new HashMap<Class, Object>();
        states.put( Lifecycle.class, LifecycleImpl.INSTANCE );

        Set<MixinResolution> usedMixins = context.getCompositeResolution().getUsedMixinModels();
        Object[] mixins = new Object[usedMixins.size()];
        int i = 0;
        for( MixinResolution resolution : usedMixins )
        {
            Object[] params = properties.get( resolution );
            MixinDependencyInjectionContext injectionContext = new MixinDependencyInjectionContext( context, composite, params, adaptContext, decorateContext );
            Object mixin = fragmentFactory.newFragment( resolution, injectionContext );
            mixins[ i++ ] = mixin;
        }

        state.setMixins( mixins );
        return state;
    }

    private void invokeCreate( T composite, CompositeInvocationHandler state )
    {
        try
        {
            state.invoke( composite, CREATE_METHOD, null );
        }
        catch( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            throw new CompositeInstantiationException( t );
        }
        catch( UndeclaredThrowableException e )
        {
            Throwable t = e.getUndeclaredThrowable();
            throw new CompositeInstantiationException( t );
        }
        catch( RuntimeException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            throw new CompositeInstantiationException( e );
        }
    }

    // Private ------------------------------------------------------
    private List getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new ArrayList();
        }
        return adaptContext;
    }

    private List getDecorateContext()
    {
        if( decorateContext == null )
        {
            decorateContext = new ArrayList();
        }
        return decorateContext;
    }
}
