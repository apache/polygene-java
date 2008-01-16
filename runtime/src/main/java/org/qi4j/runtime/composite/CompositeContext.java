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
import org.qi4j.association.AbstractAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeInstantiationException;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.Lifecycle;
import org.qi4j.property.AbstractProperty;
import org.qi4j.runtime.property.AssociationContext;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.context.ContextCompositeInstance;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.composite.MixinResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.injection.MixinInjectionContext;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.context.Context;

/**
 * TODO
 */
public final class CompositeContext
{
    private static final Method CREATE_METHOD;

    private final CompositeBinding compositeBinding;
    private final InstanceFactory instanceFactory;
    private final ModuleBinding moduleBinding;
    private final CompositeMethodInstancePool[] compositeMethodInstancePools;
    private final HashMap<Method, MethodDescriptor> methodDescriptors;
    private final Set<MixinContext> mixinContexts;
    private final Map<String, PropertyContext> propertyContexts;

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

    public CompositeContext( CompositeBinding compositeBinding, List<CompositeMethodContext> compositeMethodContexts, ModuleBinding moduleBinding, InstanceFactory instanceFactory, Map<String, PropertyContext> propertyContexts, Set<MixinContext> mixinContexts )
    {
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

    public CompositeInstance newCompositeInstance( ModuleInstance moduleInstance, Set<Object> adapt, Object decoratedObject, Map<MixinResolution, Map<PropertyContext, Object>> compositeProperties, Map<MixinResolution, Map<AssociationContext, Object>> compositeAssociations, EntitySession entitySession )
    {
        Class<? extends Composite> compositeType = getCompositeModel().getCompositeClass();
        CompositeInstance compositeInstance;
        if( Context.class.isAssignableFrom( compositeType ) )
        {
            compositeInstance = new ContextCompositeInstance( this, moduleInstance, adapt, decoratedObject, compositeProperties, compositeAssociations );
        }
        else
        {
            compositeInstance = new StandardCompositeInstance( this, moduleInstance );
        }

        // Instantiate composite proxy
        Composite proxy = newProxy( compositeInstance );
        compositeInstance.setProxy( proxy );

        // Instantiate all mixins
        newMixins( moduleInstance, compositeInstance, adapt, decoratedObject, compositeProperties, compositeAssociations );

        // Invoke lifecycle create() method
        if( proxy instanceof Lifecycle )
        {
            invokeCreate( proxy, compositeInstance );
        }

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

    /** Creates the Mixins for a Composite.
     *
     * This is not really a public method, as it is a callback needed for the ContextComposite handling.
     * @param moduleInstance The ModuleInstance that the Composite belongs to.
     * @param compositeInstance The CompositeInstance to be populated with mixins.
     * @param adaptContext A set of objects to be adapted as mixins for the Composite.
     * @param decoratedObject An objected (if any) to be decorated as a Composite.
     * @param compositeProperties The Property instances that have been established.
     * @param compositeAssociations The Association instances that have been established.
     */
    public void newMixins( ModuleInstance moduleInstance, CompositeInstance compositeInstance, Set<Object> adaptContext, Object decoratedObject, Map<MixinResolution, Map<PropertyContext, Object>> compositeProperties, Map<MixinResolution, Map<AssociationContext, Object>> compositeAssociations )
    {
        if( adaptContext == null )
        {
            adaptContext = Collections.emptySet();
        }

        if( compositeProperties == null )
        {
            compositeProperties = Collections.emptyMap();
        }

        if( compositeAssociations == null )
        {
            compositeAssociations = Collections.emptyMap();
        }

        // Calculate total set of Properties for this Composite
        Map<String, AbstractProperty> properties = new HashMap<String, AbstractProperty>();
        for( MixinContext mixinContext : mixinContexts )
        {
            Iterable<PropertyContext> mixinProperties = mixinContext.getPropertyContexts();

            MixinBinding mixinBinding = mixinContext.getMixinBinding();
            MixinResolution resolution = mixinBinding.getMixinResolution();
            Map<PropertyContext, Object> propertyValues = compositeProperties.get( resolution );
            for( PropertyContext mixinProperty : mixinProperties )
            {
                Object value;
                if( propertyValues != null && propertyValues.containsKey( mixinProperty ) )
                {
                    value = propertyValues.get( mixinProperty );
                }
                else
                {
                    value = mixinProperty.getPropertyBinding().getDefaultValue();
                }

                AbstractProperty property = mixinProperty.newInstance( moduleInstance, compositeInstance.getProxy(), value );
                PropertyBinding binding = mixinProperty.getPropertyBinding();
                PropertyResolution propertyResolution = binding.getPropertyResolution();
                PropertyModel propertyModel = propertyResolution.getPropertyModel();
                String qualifiedName = propertyModel.getQualifiedName();
                properties.put( qualifiedName, property );
            }
        }

        // Calculate total set of Associations for this Composite
        Map<String, AbstractAssociation> associations = new HashMap<String, AbstractAssociation>();
        for( MixinContext mixinContext : mixinContexts )
        {
            Iterable<AssociationContext> mixinAssociations = mixinContext.getAssociationContexts();

            MixinBinding mixinBinding = mixinContext.getMixinBinding();
            MixinResolution resolution = mixinBinding.getMixinResolution();
            Map<AssociationContext, Object> associationValues = compositeAssociations.get( resolution );
            for( AssociationContext mixinAssociation : mixinAssociations )
            {
                Object value = null;
                if( associationValues != null && associationValues.containsKey( mixinAssociation ) )
                {
                    value = associationValues.get( mixinAssociation );
                }

                AbstractAssociation association = mixinAssociation.newInstance( moduleInstance, compositeInstance.getProxy(), value );
                AssociationBinding binding = mixinAssociation.getAssociationBinding();
                AssociationResolution associationResolution = binding.getAssociationResolution();
                AssociationModel associationModel = associationResolution.getAssociationModel();
                String qualifiedName = associationModel.getQualifiedName();
                associations.put( qualifiedName, association );
            }
        }
        initializeMixins( compositeInstance, moduleInstance, adaptContext, decoratedObject, properties, associations );
    }

    private void initializeMixins( CompositeInstance compositeInstance, ModuleInstance moduleInstance, Set<Object> adaptContext, Object decoratedObject, Map<String, AbstractProperty> properties, Map<String, AbstractAssociation> associations )
    {
        Object[] mixins = compositeInstance.getMixins();
        int i = 0;
        CompositeBuilderFactory compositeBuilderFactory = moduleInstance.getCompositeBuilderFactory();
        ObjectBuilderFactory objectBuilderFactory = moduleInstance.getObjectBuilderFactory();
        ModuleBinding moduleBinding = moduleInstance.getModuleContext().getModuleBinding();
        for( MixinContext mixinContext : mixinContexts )
        {
            MixinInjectionContext injectionContext = new MixinInjectionContext( compositeBuilderFactory,
                                                                                objectBuilderFactory,
                                                                                moduleBinding,
                                                                                compositeBinding,
                                                                                compositeInstance,
                                                                                adaptContext,
                                                                                decoratedObject,
                                                                                properties,
                                                                                associations );
            Object mixin = instanceFactory.newInstance( mixinContext.getMixinBinding(), injectionContext );
            mixins[ i++ ] = mixin;
        }
    }

    private void invokeCreate( Object composite, CompositeInstance state )
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
