/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.composite;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.AccessibleObjects;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.injection.Dependencies;
import org.apache.polygene.runtime.injection.DependencyModel;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.polygene.api.composite.InvalidCompositeException.handleInvalidCompositeType;

/**
 * JAVADOC
 */
public abstract class CompositeModel
    implements VisitableHierarchy<Object, Object>, Dependencies, CompositeDescriptor
{
    protected final ModuleDescriptor module;
    protected final MixinsModel mixinsModel;
    protected final CompositeMethodsModel compositeMethodsModel;
    protected final StateModel stateModel;

    private final Class<?> primaryType;
    private final Set<Class<?>> types;
    private final Visibility visibility;
    private final MetaInfo metaInfo;

    private Class<? extends Composite> proxyClass;
    private Constructor<? extends Composite> proxyConstructor;

    protected CompositeModel( final ModuleDescriptor module,
                              final List<Class<?>> types,
                              final Visibility visibility,
                              final MetaInfo metaInfo,
                              final MixinsModel mixinsModel,
                              final StateModel stateModel,
                              final CompositeMethodsModel compositeMethodsModel
    )
    {
        this.module = module;
        this.types = new LinkedHashSet<>( types );
        this.visibility = visibility;
        this.metaInfo = metaInfo;
        this.stateModel = stateModel;
        this.compositeMethodsModel = compositeMethodsModel;
        this.mixinsModel = mixinsModel;

        // Create proxy class
        @SuppressWarnings( "OptionalGetWithoutIsPresent" )
        Class<?> mainType = types.stream().findFirst().get();
        try
        {
            proxyClass = createProxyClass( mainType );
            proxyConstructor = createProxyConstructor( mainType );
        }
        catch( ClassNotFoundException | NoSuchMethodException e )
        {
            handleInvalidCompositeType( e.getMessage(), module, mainType, null, null, null, types );
        }
        primaryType = mixinTypes()
            .reduce( null, ( primary, type ) ->
            {
                if( primary == null )
                {
                    return type;
                }
                else if( primary.isAssignableFrom( type ) )
                {
                    return type;
                }
                return primary;
            } );
    }

    // Model
    @Override
    public Stream<Class<?>> types()
    {
        return types.stream();
    }

    public StateModel state()
    {
        return stateModel;
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    @Override
    public Visibility visibility()
    {
        return visibility;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        for( Class<?> aClass : types )
        {
            if( type.isAssignableFrom( aClass ) )
            {
                return true;
            }
        }
        return false;
    }

    public MixinsModel mixinsModel()
    {
        return mixinsModel;
    }

    @Override
    @SuppressWarnings( { "raw", "unchecked" } )
    public Class<?> primaryType()
    {
        return primaryType;
    }

    @Override
    public Stream<Class<?>> mixinTypes()
    {
        return mixinsModel.mixinTypes();
    }

    @Override
    public Stream<DependencyModel> dependencies()
    {
        Stream<Dependencies> models = Stream.of( this.mixinsModel, compositeMethodsModel );
        return models.flatMap( Dependencies::dependencies );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( compositeMethodsModel.accept( visitor ) )
            {
                if( stateModel.accept( visitor ) )
                {
                    mixinsModel.accept( visitor );
                }
            }
        }
        return visitor.visitLeave( this );
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    private Class<? extends Composite>  createProxyClass( Class<?> mainType )
        throws ClassNotFoundException, NoSuchMethodException
    {
        Class<? extends Composite>  proxyClass;
        if( mainType.isInterface() )
        {
            ClassLoader proxyClassloader = mainType.getClassLoader();

            Class<?>[] interfaces = types.stream().map( Class.class::cast ).toArray( Class[]::new );
            proxyClass = (Class<? extends Composite>) ProxyGenerator.createProxyClass( proxyClassloader, interfaces );
        }
        else
        {
            proxyClass = new TransientClassLoader( getClass().getClassLoader() ).loadFragmentClass( mainType );
        }
        return proxyClass;
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    private Constructor<? extends Composite> createProxyConstructor( Class<?> mainType )
        throws ClassNotFoundException, NoSuchMethodException
    {
        Constructor<? extends Composite> constructor;
        if( mainType.isInterface() )
        {
            constructor = proxyClass.getConstructor( InvocationHandler.class );
        }
        else
        {
            constructor = (Constructor<? extends Composite>) proxyClass.getConstructors()[ 0 ];
        }
        return AccessibleObjects.accessible( constructor );
    }

    // Context
    public final Object invoke( MixinsInstance mixins,
                                Object proxy,
                                Method method,
                                Object[] args
    )
        throws Throwable
    {
        return compositeMethodsModel.invoke( mixins, proxy, method, args, module );
    }

    @Override
    public ModuleDescriptor module()
    {
        return module;
    }

    public Composite newProxy( InvocationHandler invocationHandler )
        throws ConstructionException
    {
        Class<?> mainType = types.stream().findFirst().get();
        if( mainType.isInterface() )
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
        else
        {
            try
            {
                Object[] args = new Object[ proxyConstructor.getParameterTypes().length ];
                Composite composite = Composite.class.cast( proxyConstructor.newInstance( args ) );
                proxyClass.getField( "_instance" ).set( composite, invocationHandler );
                return composite;
            }
            catch( Exception e )
            {
                throw new ConstructionException( e );
            }
        }
    }

    @SuppressWarnings( "raw" )
    public <T> T newProxy( InvocationHandler invocationHandler, Class<T> mixinType )
        throws IllegalArgumentException
    {

//        if (!matchesAny( isAssignableFrom( mixinType ), types ))
        if( !mixinsModel.isImplemented( mixinType ) )
        {
            String message = "Composite " + primaryType().getName() + " does not implement type " + mixinType.getName();
            throw new IllegalArgumentException( message );
        }

        // Instantiate proxy for given mixin interface
        return mixinType.cast( newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, invocationHandler ) );
    }

    @Override
    public String toString()
    {
        return primaryType.getSimpleName();
    }
}