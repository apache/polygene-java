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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.composite.Composite;
import org.qi4j.composite.State;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.injection.DependencyVisitor;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public class CompositeModel
    implements Binder, CompositeDescriptor
{
    public static String toURI( final Class<? extends Composite> compositeClass )
    {
        if( compositeClass == null )
        {
            return null;
        }
        String className = compositeClass.getName();
        className = className.replace( '$', '&' );
        return "urn:qi4j:" + className;
    }

    public static CompositeModel newModel( Class<? extends Composite> type, Visibility visibility, MetaInfo info, ModuleModel moduleModel )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( type );
        MixinsModel mixinsModel = new MixinsModel( type );
        StateModel stateModel = new StateModel( new PropertiesModel( constraintsModel ) );
        ConcernsModel concernsModel = new ConcernsModel( type );
        SideEffectsModel sideEffectsModel = new SideEffectsModel( type );
        CompositeMethodsModel compositeMethodsModel = new CompositeMethodsModel( type, constraintsModel, concernsModel, sideEffectsModel, mixinsModel );

        return new CompositeModel( type,
                                   visibility,
                                   info,
                                   moduleModel,
                                   constraintsModel,
                                   concernsModel,
                                   sideEffectsModel,
                                   mixinsModel,
                                   stateModel,
                                   compositeMethodsModel );
    }

    private ConstraintsModel constraintsModel;
    private ConcernsModel concernsModel;
    private SideEffectsModel sideEffectsModel;
    private MixinsModel mixinsModel;
    private CompositeMethodsModel compositeMethodsModel;
    private Class<? extends Composite> compositeType;
    private Visibility visibility;
    private MetaInfo metaInfo;
    private ModuleModel moduleModel;
    private StateModel stateModel;
    private Class<? extends Composite> proxyClass;

    public CompositeModel( Class<? extends Composite> compositeType,
                           Visibility visibility,
                           MetaInfo metaInfo,
                           ModuleModel moduleModel,
                           ConstraintsModel constraintsModel,
                           ConcernsModel concernsModel,
                           SideEffectsModel sideEffectsModel,
                           MixinsModel mixinsModel,
                           StateModel stateModel,
                           CompositeMethodsModel compositeMethodsModel
    )
    {
        this.compositeType = compositeType;
        this.visibility = visibility;
        this.metaInfo = metaInfo;
        this.moduleModel = moduleModel;
        this.stateModel = stateModel;

        // Create proxy class
        this.proxyClass = createProxyClass( compositeType );

        this.constraintsModel = constraintsModel;
        this.concernsModel = concernsModel;
        this.sideEffectsModel = sideEffectsModel;
        this.mixinsModel = mixinsModel;

        this.compositeMethodsModel = compositeMethodsModel;

        mixinsModel.implementThisUsing( this );
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

    private Class<? extends Composite> createProxyClass( Class<? extends Composite> compositeType )
    {
        ClassLoader proxyClassloader = compositeType.getClassLoader();
        Class[] interfaces = new Class[]{ compositeType };
        return (Class<? extends Composite>) Proxy.getProxyClass( proxyClassloader, interfaces );
    }

    public void visitDependencies( DependencyVisitor visitor )
    {
        concernsModel.visitDependencies( visitor );
        sideEffectsModel.visitDependencies( visitor );
        mixinsModel.visitDependencies( visitor );
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );
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
        throws org.qi4j.composite.InstantiationException
    {
        // Instantiate proxy for given composite interface
        try
        {
            return Composite.class.cast( proxyClass.getConstructor( InvocationHandler.class ).newInstance( invocationHandler ) );
        }
        catch( Exception e )
        {
            throw new org.qi4j.composite.InstantiationException( e );
        }
    }

    public CompositeInstance newCompositeInstance( ModuleInstance moduleInstance,
                                                   UsesInstance uses,
                                                   State state )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        DefaultCompositeInstance compositeInstance = new DefaultCompositeInstance( this, moduleInstance, mixins );

        // Instantiate all mixins
        mixinsModel.newMixins( compositeInstance,
                               uses,
                               state,
                               mixins );

        // Invoke lifecycle create() method
/*
        if( proxy instanceof Lifecycle )
        {
            invokeCreate( proxy, compositeInstance );
        }
*/

        // Return
        return compositeInstance;
    }

    public void implementMixinType( Class mixinType )
    {
        compositeMethodsModel.implementMixinType( mixinType );
        stateModel.addStateFor( mixinType );
    }

    public State newDefaultState()
    {
        return stateModel.newDefaultInstance();
    }

    public String toURI()
    {
        return toURI( compositeType );
    }
}