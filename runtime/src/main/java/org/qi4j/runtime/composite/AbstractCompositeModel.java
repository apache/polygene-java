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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public abstract class AbstractCompositeModel
    implements Binder, Serializable
{
    protected final AbstractMixinsModel mixinsModel;
    protected final CompositeMethodsModel compositeMethodsModel;
    private final Class<? extends Composite> compositeType;
    private final Visibility visibility;
    private final MetaInfo metaInfo;
    protected final AbstractStateModel stateModel;
    protected final Class<? extends Composite> proxyClass;

    protected AbstractCompositeModel( final Class<? extends Composite> compositeType,
                                      final Visibility visibility,
                                      final MetaInfo metaInfo,
                                      final AbstractMixinsModel mixinsModel,
                                      final AbstractStateModel stateModel,
                                      final CompositeMethodsModel compositeMethodsModel
    )
    {
        this.compositeType = compositeType;
        this.visibility = visibility;
        this.metaInfo = metaInfo;
        this.stateModel = stateModel;

        // Create proxy class
        this.proxyClass = createProxyClass( compositeType );

        this.mixinsModel = mixinsModel;

        this.compositeMethodsModel = compositeMethodsModel;
    }

    // Model

    public Class<? extends Composite> type()
    {
        return compositeType;
    }

    public AbstractStateModel state()
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

    public Class<? extends Composite> proxyClass()
    {
        return proxyClass;
    }

    public Iterable<Class> mixinTypes()
    {
        return mixinsModel.mixinTypes();
    }

    public CompositeMethodsModel compositeMethodsModel()
    {
        return compositeMethodsModel;
    }

    @SuppressWarnings( "unchecked" )
    private Class<? extends Composite> createProxyClass( Class<? extends Composite> compositeType )
    {
        ClassLoader proxyClassloader = compositeType.getClassLoader();
        Class[] interfaces = new Class[]{ compositeType };
        return (Class<? extends Composite>) Proxy.getProxyClass( proxyClassloader, interfaces );
    }

    public abstract void visitModel( ModelVisitor modelVisitor );

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

    public final Object getMixin( Object[] mixins, Method method )
    {
        return mixinsModel.getMixin( mixins, method );
    }

    public Composite newProxy( InvocationHandler invocationHandler )
        throws ConstructionException
    {
        // Instantiate proxy for given composite interface
        try
        {
            return Composite.class.cast( proxyClass.getConstructor( InvocationHandler.class ).newInstance( invocationHandler ) );
        }
        catch( Exception e )
        {
            throw new ConstructionException( e );
        }
    }

    public <T> T newProxy( InvocationHandler invocationHandler, Class<T> mixinType )
    {
        // Instantiate proxy for given mixin interface
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, invocationHandler ) );
    }

    public StateHolder newBuilderState()
    {
        return stateModel.newBuilderInstance();
    }

    public StateHolder newBuilderState( StateHolder state )
    {
        return stateModel.newBuilderInstance( state );
    }

    public StateHolder newInitialState()
    {
        return stateModel.newInitialInstance();
    }

    public StateHolder newState( StateHolder state )
    {
        return stateModel.newInstance( state );
    }

    @Override
    public String toString()
    {
        return compositeType.getName();
    }
}