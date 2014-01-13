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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.toList;

/**
 * JAVADOC
 */
public abstract class CompositeModel
    implements VisitableHierarchy<Object, Object>, Dependencies, CompositeDescriptor
{
    protected final MixinsModel mixinsModel;
    protected final CompositeMethodsModel compositeMethodsModel;
    private final Set<Class<?>> types;
    private final Visibility visibility;
    private final MetaInfo metaInfo;
    protected final StateModel stateModel;
    protected Class<? extends Composite> proxyClass;
    protected Constructor<? extends Composite> proxyConstructor;

    protected CompositeModel( final Iterable<Class<?>> types,
                              final Visibility visibility,
                              final MetaInfo metaInfo,
                              final MixinsModel mixinsModel,
                              final StateModel stateModel,
                              final CompositeMethodsModel compositeMethodsModel
    )
    {
        this.types = Iterables.addAll( new LinkedHashSet<Class<?>>(), types );
        this.visibility = visibility;
        this.metaInfo = metaInfo;
        this.stateModel = stateModel;
        this.compositeMethodsModel = compositeMethodsModel;
        this.mixinsModel = mixinsModel;

        // Create proxy class
        createProxyClass();
    }

    // Model
    @Override
    public Iterable<Class<?>> types()
    {
        return types;
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

    @Override
    @SuppressWarnings( {"raw", "unchecked"} )
    public Class<?> primaryType()
    {
        Class primaryType = null;
        for( Class type : mixinTypes() )
        {
            if( type.getName().equals( "scala.ScalaObject" ) )
            {
                continue;
            }
            if( primaryType == null )
            {
                primaryType = type;
            }
            else if( primaryType.isAssignableFrom( type ) )
            {
                primaryType = type;
            }
        }
        return primaryType;
    }

    @Override
    public Iterable<Class<?>> mixinTypes()
    {
        return mixinsModel.mixinTypes();
    }

    @Override
    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flatten( mixinsModel.dependencies(), compositeMethodsModel.dependencies() );
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

    @SuppressWarnings( {"raw", "unchecked"} )
    private void createProxyClass()
    {
        Class<?> mainType = first( types );
        if( mainType.isInterface() )
        {
            ClassLoader proxyClassloader = mainType.getClassLoader();

            Class<?>[] interfaces = Iterables.toArray( Class.class, Iterables.<Class>cast( types ) );
            proxyClass = (Class<? extends Composite>) ProxyGenerator.createProxyClass( proxyClassloader, interfaces );

            try
            {
                proxyConstructor = proxyClass.getConstructor( InvocationHandler.class );
            }
            catch( NoSuchMethodException e )
            {
                throw (InvalidCompositeException) new InvalidCompositeException( "Could not get proxy constructor" ).initCause( e );
            }
            proxyConstructor.setAccessible( true );
        }
        else
        {
            try
            {
                proxyClass = new TransientClassLoader( getClass().getClassLoader() ).loadFragmentClass( mainType );
                proxyConstructor = (Constructor<? extends Composite>) proxyClass.getConstructors()[ 0 ];
            }
            catch( ClassNotFoundException e )
            {
                throw (InvalidCompositeException) new InvalidCompositeException( "Could not get proxy constructor" ).initCause( e );
            }
        }
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
        Class<?> mainType = first( types() );
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
            throw new IllegalArgumentException( "Composite does not implement type " + mixinType.getName() );
        }

        // Instantiate proxy for given mixin interface
        return mixinType.cast( newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, invocationHandler ) );
    }

    @Override
    public String toString()
    {
        return toList( types ).toString();
    }
}