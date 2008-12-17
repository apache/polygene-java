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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.api.util.ClassUtil;

/**
 * TODO
 */
public final class CompositeModel
    implements Binder, CompositeDescriptor
{
    private static final Method LIFECYCLE_CREATE;

    static
    {
        try
        {
            LIFECYCLE_CREATE = Lifecycle.class.getMethod( "create" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: Lifeycle" );
        }
    }

    public static CompositeModel newModel( final Class<? extends Composite> compositeType,
                                           final Visibility visibility,
                                           final MetaInfo metaInfo,
                                           final PropertyDeclarations propertyDeclarations )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( compositeType );
        StateModel stateModel = new StateModel( new PropertiesModel( constraintsModel, propertyDeclarations ) );
        MixinsModel mixinsModel = new MixinsModel( compositeType, stateModel );
        ConcernsDeclaration concernsModel = new ConcernsDeclaration( compositeType );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( compositeType );
        CompositeMethodsModel compositeMethodsModel =
            new CompositeMethodsModel( compositeType, constraintsModel, concernsModel, sideEffectsModel, mixinsModel );

        return new CompositeModel(
            compositeType, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    private final MixinsModel mixinsModel;
    private final CompositeMethodsModel compositeMethodsModel;
    private final Class<? extends Composite> compositeType;
    private final Visibility visibility;
    private final MetaInfo metaInfo;
    private final StateModel stateModel;
    private final Class<? extends Composite> proxyClass;

    private CompositeModel( final Class<? extends Composite> compositeType,
                            final Visibility visibility,
                            final MetaInfo metaInfo,
                            final MixinsModel mixinsModel,
                            final StateModel stateModel,
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

    public StateDescriptor state()
    {
        return stateModel;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
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

    @SuppressWarnings( "unchecked" )
    private Class<? extends Composite> createProxyClass( Class<? extends Composite> compositeType )
    {
        ClassLoader proxyClassloader = compositeType.getClassLoader();
        Class[] interfaces = new Class[]{ compositeType };
        return (Class<? extends Composite>) Proxy.getProxyClass( proxyClassloader, interfaces );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositeMethodsModel.visitModel( modelVisitor );
        mixinsModel.visitModel( modelVisitor );
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null, null );
        compositeMethodsModel.bind( resolution );
        mixinsModel.bind( resolution );
    }

    // Context

    public Object invoke( Object composite, Object[] params, Object[] mixins, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        return mixinsModel.invoke( composite, params, mixins, methodInstance );
    }

    public Object invoke( MixinsInstance mixins, Object proxy, Method method, Object[] args, ModuleInstance moduleInstance )
        throws Throwable
    {
        return compositeMethodsModel.invoke( mixins, proxy, method, args, moduleInstance );
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

    public CompositeInstance newCompositeInstance( ModuleInstance moduleInstance,
                                                   UsesInstance uses,
                                                   StateHolder state )
    {
        stateModel.checkConstraints( state );

        Object[] mixins = mixinsModel.newMixinHolder();
        DefaultCompositeInstance compositeInstance = new DefaultCompositeInstance( this, moduleInstance, mixins, state );

        try
        {
            // Instantiate all mixins
            mixinsModel.newMixins( compositeInstance,
                                   uses,
                                   state,
                                   mixins );

        }
        catch( InvalidCompositeException e )
        {
            e.setFailingCompositeType( compositeType );
            e.setMessage( "Invalid Cyclic Mixin usage dependency"  );
            throw e;
        }
        // Invoke lifecycle create() method
        Composite proxy = compositeInstance.proxy();
        if( proxy instanceof Lifecycle )
        {
            invokeCreate( proxy );
        }

        // Return
        return compositeInstance;
    }

    private void invokeCreate( Composite proxy )
    {
        try
        {
            LIFECYCLE_CREATE.invoke( proxy );
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace();
        }
        catch( InvocationTargetException e )
        {
            e.printStackTrace();
        }
    }

    public StateHolder newBuilderState()
    {
        return stateModel.newBuilderState();
    }

    public StateHolder newDefaultState()
    {
        return stateModel.newDefaultInstance();
    }

    public StateHolder newState( StateHolder state )
    {
        return stateModel.newState( state );
    }

    public String toURI()
    {
        return ClassUtil.toURI( compositeType );
    }

    @Override public String toString()
    {
        return compositeType.getName();
    }

}