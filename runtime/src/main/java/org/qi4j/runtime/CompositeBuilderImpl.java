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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.IllegalMixinTypeException;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.runtime.LifecycleImpl;
import org.qi4j.spi.TypeLookupResolver;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    private static final Method CREATE_METHOD;
    protected Map<Class, Object> states;
    private CompositeModelFactory modelFactory;
    protected CompositeBuilderFactoryImpl builderFactory;
    protected Class<T> compositeInterface;
    private CompositeContextImpl context;
    private CompositeInvocationHandler state;
    private T composite;
    private TypeLookupResolver resolver;

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

    CompositeBuilderImpl( CompositeContextImpl<T> context )
    {
        this.context = context;
        this.modelFactory = context.getCompositeModelFactory();
        this.builderFactory = (CompositeBuilderFactoryImpl) context.getCompositeBuilderFactory();
        this.compositeInterface = context.getCompositeModel().getCompositeClass();
        composite = builderFactory.newInstance( compositeInterface );
        state = CompositeInvocationHandler.getInvocationHandler( composite );
        states = new HashMap<Class, Object>();
        states.put( Lifecycle.class, new LifecycleImpl() );
    }

    public T newInstance()
    {
        state.setMixins( states, false );
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
        return composite;
    }

    public <K> void setMixin( Class<K> mixinType, K mixin )
    {
        if( mixinType.isAssignableFrom( compositeInterface ) )
        {
            if( mixinType.isInstance( mixin ) )
            {
                states.put( mixinType, mixin );
            }
            else
            {
                throw new IllegalMixinTypeException( mixin.getClass().getName() + " is not an implementation of " + mixinType.getName() );
            }
        }
        else
        {
            throw new IllegalMixinTypeException( mixinType.getName() + " is not a superinterface of " + compositeInterface.getName() );
        }

    }

    public void setMixin( Class mixinType, InvocationHandler mixin )
    {
        states.put( mixinType, mixin );
    }

    public <K> K getMixin( Class<K> mixinType )
    {
        Object mixin = states.get( mixinType );
        if( mixin == null )
        {
            CompositeModel model = modelFactory.getCompositeModel( compositeInterface );
            List<MixinModel> mixinModels = model.getImplementations( mixinType );
            if( mixinModels.size() == 0 )
            {
                return null;
            }
            MixinModel mixinModel = mixinModels.get( 0 );
            mixin = context.newFragment( mixinModel, composite, null );
            states.put( mixinType, mixin );
        }
        if( mixin instanceof InvocationHandler )
        {
            mixin = Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[] { mixinType }, (InvocationHandler) mixin );
        }
        return mixinType.cast( mixin );
    }

    public void adapt( Object mixin )
    {
        CompositeModel model = modelFactory.getCompositeModel( compositeInterface );
        Set<Class> unresolved = model.getUnresolved();
        for( Class needed : unresolved )
        {
            if( needed.isInstance( mixin ) )
            {
                setMixin( needed, needed.cast( mixin ) );
            }
        }
    }

    public <K> void provideDependency( Class<K> dependencyType, K dependencyInstance )
    {
        if( resolver == null )
        {
            resolver = new TypeLookupResolver();
        }
        resolver.put( dependencyType, dependencyInstance );
    }

    public void provideDependency( Object dependencyInstance )
    {
        Class[] intfaces = dependencyInstance.getClass().getInterfaces();
        provideDependencyRecursion( intfaces, dependencyInstance );

    }

    private void provideDependencyRecursion( Class[] intfaces, Object dependencyInstance )
    {
        for( Class intface : intfaces )
        {
            provideDependency( intface, dependencyInstance );
            provideDependencyRecursion( intface.getInterfaces(), dependencyInstance );
        }
    }
}
