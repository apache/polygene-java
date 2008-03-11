/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeInstantiationException;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.Lifecycle;
import org.qi4j.property.Property;
import org.qi4j.runtime.property.AssociationContext;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.injection.MixinInjectionContext;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class CompositeContext
{
    private static final Set<Object> EMPTY_SET = new HashSet<Object>();
    private static final Method CREATE_METHOD;

    private CompositeBinding compositeBinding;
    private InstanceFactory instanceFactory;
    private ModuleBinding moduleBinding;
    private CompositeMethodInstancePool[] compositeMethodInstancePools;
    private HashMap<Method, MethodDescriptor> methodDescriptors;
    private Set<MixinContext> mixinContexts;
    private Map<String, PropertyContext> propertyContexts;
    private Map<String, AssociationContext> associationContexts;

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

    public CompositeContext( CompositeBinding compositeBinding, List<CompositeMethodContext> compositeMethodContexts, ModuleBinding moduleBinding, InstanceFactory instanceFactory, Map<String, PropertyContext> propertyContexts, Set<MixinContext> mixinContexts, Map<String, AssociationContext> associationContexts )
    {
        this.associationContexts = associationContexts;
        this.mixinContexts = mixinContexts;
        this.propertyContexts = propertyContexts;
        this.moduleBinding = moduleBinding;
        this.compositeBinding = compositeBinding;
        this.instanceFactory = instanceFactory;

        // Create index of method to mixin and invocation instance pools
        methodDescriptors = new HashMap<Method, MethodDescriptor>();
        Map<MixinBinding, Integer> mixinIndices = new HashMap<MixinBinding, Integer>();
        compositeMethodInstancePools = new CompositeMethodInstancePool[compositeBinding.getCompositeMethodBindings().size()];

        // Assign index to each mixin binding
        int currentMixinIndex = 0;
        for( MixinContext mixinContext : mixinContexts )
        {
            mixinIndices.put( mixinContext.getMixinBinding(), currentMixinIndex++ );
        }

        int methodIndex = 0;
        for( CompositeMethodContext compositeMethodContext : compositeMethodContexts )
        {
            MixinBinding mixinBinding = compositeMethodContext.getCompositeMethodBinding().getMixinBinding();
            int index = mixinIndices.get( mixinBinding );
            compositeMethodInstancePools[ methodIndex ] = new CompositeMethodInstancePool();
            MethodDescriptor methodDescriptor = new MethodDescriptor( compositeMethodContext, methodIndex, index, compositeMethodInstancePools[ methodIndex ] );
            Method method = compositeMethodContext.getCompositeMethodBinding().getCompositeMethodResolution().getCompositeMethodModel().getMethod();
            methodDescriptors.put( method, methodDescriptor );
//            System.out.println( this + " : [" + index + "] : [" +methodIndex+"]  -> " + methodDescriptor.getCompositeMethodContext().getCompositeMethodBinding() );
            methodIndex++;
        }

    }

    public CompositeModel getCompositeModel()
    {
        return compositeBinding.getCompositeResolution().getCompositeModel();
    }

    public CompositeResolution getCompositeResolution()
    {
        return compositeBinding.getCompositeResolution();
    }

    public CompositeBinding getCompositeBinding()
    {
        return compositeBinding;
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }

    public CompositeInstance newCompositeInstance( ModuleInstance moduleInstance, Set<Object> adapt, Object decoratedObject, Map<String, Property> compositeProperties, Map<String, AbstractAssociation> compositeAssociations )
    {
        CompositeInstance compositeInstance = new StandardCompositeInstance( this, moduleInstance );

        // Instantiate composite proxy
        Composite proxy = newProxy( compositeInstance );
        compositeInstance.setProxy( proxy );

        // Instantiate all mixins
        Object[] mixins = new Object[mixinContexts.size()];
        compositeInstance.setMixins( mixins );
        newMixins( moduleInstance, compositeInstance, adapt, decoratedObject, compositeProperties, compositeAssociations, mixins );

        // Invoke lifecycle create() method
        if( proxy instanceof Lifecycle )
        {
            invokeCreate( proxy, compositeInstance );
        }

        // Return
        return compositeInstance;
    }

    public EntityCompositeInstance newEntityCompositeInstance( ModuleInstance moduleInstance, EntitySession session, EntityStore store, String identity )
    {
        EntityCompositeInstance compositeInstance = new EntityCompositeInstance( session, this, moduleInstance, store, identity );

        // Instantiate composite proxy
        Composite proxy = newProxy( compositeInstance );
        compositeInstance.setProxy( proxy );

        // Return
        return compositeInstance;
    }

    public MethodDescriptor getMethodDescriptor( Method method )
    {
        return methodDescriptors.get( method );
    }

    public CompositeMethodInstance getMethodInstance( MethodDescriptor methodDescriptor, ModuleInstance moduleInstance )
    {
        CompositeMethodInstancePool instances = methodDescriptor.getMethodInstances();

        CompositeMethodInstance instance = instances.getInstance();

        if( instance == null )
        {
            CompositeMethodContext compositeMethodContext = methodDescriptor.getCompositeMethodContext();
            instance = compositeMethodContext.newCompositeMethodInstance( moduleInstance, instances );
        }

        return instance;
    }

    public PropertyContext getPropertyContext( Class mixinType, String name )
    {
        return propertyContexts.get( mixinType.getName() + ":" + name );
    }

    public Iterable<PropertyContext> getPropertyContexts()
    {
        return propertyContexts.values();
    }

    public Iterable<AssociationContext> getAssociationContexts()
    {
        return associationContexts.values();
    }

    private Composite newProxy( CompositeInstance handler )
        throws CompositeInstantiationException
    {
        // Instantiate proxy for given composite interface
        try
        {
            CompositeModel compositeModel = handler.getContext().getCompositeBinding().getCompositeResolution().getCompositeModel();
            Class proxyClass = compositeModel.getProxyClass();
            return Composite.class.cast( proxyClass.getConstructor( InvocationHandler.class ).newInstance( handler ) );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( e );
        }
    }

    public void newEntityMixins( ModuleInstance moduleInstance, EntityCompositeInstance compositeInstance, EntityState state )
    {
        Object[] mixins = new Object[mixinContexts.size()];
        compositeInstance.setMixins( mixins );
        newMixins( moduleInstance, compositeInstance, Collections.emptySet(), null, state.getProperties(), state.getAssociations(), mixins );
    }

    public void newMixins( ModuleInstance moduleInstance, CompositeInstance compositeInstance, Set adaptContext, Object decoratedObject, Map<String, Property> compositeProperties, Map<String, AbstractAssociation> compositeAssociations, Object[] mixins )
    {
        Set<Object> adapt = adaptContext == null ? EMPTY_SET : adaptContext;

        int i = 0;
        for( MixinContext mixinContext : mixinContexts )
        {
            MixinInjectionContext injectionContext = new MixinInjectionContext( moduleInstance.getCompositeBuilderFactory(),
                                                                                moduleInstance.getObjectBuilderFactory(),
                                                                                moduleInstance.getServiceRegistry(),
                                                                                moduleInstance.getModuleContext().getModuleBinding(),
                                                                                compositeBinding,
                                                                                compositeInstance,
                                                                                adapt,
                                                                                decoratedObject,
                                                                                compositeProperties,
                                                                                compositeAssociations );
            Object mixin = instanceFactory.newInstance( mixinContext.getMixinBinding(), injectionContext );
            mixins[ i++ ] = mixin;
        }
    }

    public void invokeCreate( Object composite, CompositeInstance state )
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
}
