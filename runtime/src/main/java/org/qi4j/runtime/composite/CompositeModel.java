/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JAVADOC
 */
public abstract class CompositeModel
    implements VisitableHierarchy<Object, Object>, Dependencies, CompositeDescriptor
{
    protected final MixinsModel mixinsModel;
    protected final CompositeMethodsModel compositeMethodsModel;
    private final Class<?> type;
    private final Iterable<Class<?>> types;
    private final Visibility visibility;
    private final MetaInfo metaInfo;
    protected final StateModel stateModel;
    protected final Class<? extends Composite> proxyClass;
    protected Constructor<? extends Composite> proxyConstructor;

    protected CompositeModel( final Class<?> type,
                              final Iterable<Class<?>> types,
                              final Visibility visibility,
                              final MetaInfo metaInfo,
                              final MixinsModel mixinsModel,
                              final StateModel stateModel,
                              final CompositeMethodsModel compositeMethodsModel
    )
    {
        this.type = type;
        this.types = types;
        this.visibility = visibility;
        this.metaInfo = metaInfo;
        this.stateModel = stateModel;

        this.mixinsModel = mixinsModel;

        // Create proxy class
        this.proxyClass = createProxyClass( types );
        try
        {
            proxyConstructor = proxyClass.getConstructor( InvocationHandler.class );
        } catch( NoSuchMethodException e )
        {
            throw (InvalidCompositeException) new InvalidCompositeException( "Could not get proxy constructor").initCause( e );
        }
        proxyConstructor.setAccessible( true );

        this.compositeMethodsModel = compositeMethodsModel;
    }

    // Model
    public Iterable<Class<?>> types()
    {
        return types;
    }

    public StateModel state()
    {
        return stateModel;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public Visibility visibility()
    {
        return visibility;
    }


    @Override
    public Class<?> type()
    {
        return type;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        for( Class<?> aClass : types )
        {
            if (type.isAssignableFrom( aClass ))
                return true;
        }
        return false;
    }

    public Iterable<Class<?>> mixinTypes()
    {
        return mixinsModel.mixinTypes();
    }

    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flatten( mixinsModel.dependencies(), compositeMethodsModel.dependencies() );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor ) throws ThrowableType
    {
        if (visitor.visitEnter( this ))
        {
            if (compositeMethodsModel.accept( visitor ))
                if (((VisitableHierarchy<Object, Object>)stateModel).accept( visitor ))
                    mixinsModel.accept(visitor);
        }
        return visitor.visitLeave( this );
    }

    @SuppressWarnings( "unchecked" )
    private Class<? extends Composite> createProxyClass( Iterable<Class<?>> types)
    {
        ClassLoader proxyClassloader = Iterables.first( types ).getClassLoader();

        Class<?>[] interfaces = Iterables.toArray( Class.class, Iterables.<Class, Class<?>>cast( types) );
        return (Class<? extends Composite>) Proxy.getProxyClass( proxyClassloader, interfaces );
    }

    // Context
    public final Object invoke( MixinsInstance mixins,
                                Object proxy,
                                Method method,
                                Object[] args,
                                ModuleInstance moduleInstance
    )
        throws Throwable
    {
        return compositeMethodsModel.invoke( mixins, proxy, method, args, moduleInstance );
    }

    public Composite newProxy( InvocationHandler invocationHandler )
        throws ConstructionException
    {
        try
        {
            return Composite.class.cast( proxyConstructor.newInstance( invocationHandler ) );
        }
        catch( Exception e )
        {
            throw new ConstructionException( e );
        }
    }

    public <T> T newProxy( InvocationHandler invocationHandler, Class<T> mixinType )
    {
        // Instantiate proxy for given mixin interface
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{mixinType}, invocationHandler ) );
    }

    public StateHolder newBuilderState( final ModuleInstance module )
    {
        return stateModel.newBuilderInstance( new Function<PropertyDescriptor, Object>()
        {
            @Override
            public Object map( PropertyDescriptor propertyDescriptor )
            {
                return propertyDescriptor.initialValue( module );
            }
        }  );
    }

    public StateHolder newBuilderState( Function<PropertyDescriptor, Object> state )
    {
        return stateModel.newBuilderInstance( state );
    }

    public StateHolder newInitialState( ModuleInstance module )
    {
        return stateModel.newInitialInstance(module);
    }

    public StateHolder newState( StateHolder state )
    {
        return stateModel.newInstance( state );
    }

    @Override
    public String toString()
    {
        return Iterables.toList( types ).toString();
    }
}