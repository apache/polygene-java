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
package org.apache.polygene.runtime.service;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.runtime.activation.ActivatorsInstance;
import org.apache.polygene.runtime.activation.ActivatorsModel;
import org.apache.polygene.runtime.composite.CompositeMethodsModel;
import org.apache.polygene.runtime.composite.CompositeModel;
import org.apache.polygene.runtime.composite.MixinModel;
import org.apache.polygene.runtime.composite.MixinsModel;
import org.apache.polygene.runtime.composite.StateModel;
import org.apache.polygene.runtime.composite.TransientStateInstance;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.DependencyModel;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.property.PropertyInstance;

/**
 * JAVADOC
 */
public final class ServiceModel extends CompositeModel
    implements ServiceDescriptor
{
    private final Identity identity;
    private final boolean instantiateOnStartup;
    private final ActivatorsModel<?> activatorsModel;
    private final Class configurationType;

    public ServiceModel( ModuleDescriptor module,
                         List<Class<?>> types,
                         Visibility visibility,
                         MetaInfo metaInfo,
                         ActivatorsModel<?> activatorsModel,
                         MixinsModel mixinsModel,
                         StateModel stateModel,
                         CompositeMethodsModel compositeMethodsModel,
                         Identity identity,
                         boolean instantiateOnStartup
    )
    {
        super( module, types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

        this.identity = identity;
        this.instantiateOnStartup = instantiateOnStartup;
        this.activatorsModel = activatorsModel;

        // Calculate configuration type
        this.configurationType = calculateConfigurationType();
    }

    @Override
    public boolean isInstantiateOnStartup()
    {
        return instantiateOnStartup;
    }

    @Override
    public Identity identity()
    {
        return identity;
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    public ActivatorsInstance<?> newActivatorsInstance( ModuleDescriptor module )
        throws Exception
    {
        return new ActivatorsInstance( activatorsModel.newInstances( module ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Class<T> configurationType()
    {
        return configurationType;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( activatorsModel.accept( visitor ) )
            {
                if( compositeMethodsModel.accept( visitor ) )
                {
                    if( stateModel.accept( visitor ) )
                    {
                        mixinsModel.accept( visitor );
                    }
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public ServiceInstance newInstance( final ModuleDescriptor module )
    {
        Object[] mixins = mixinsModel.newMixinHolder();

        Map<AccessibleObject, Property<?>> properties = new HashMap<>();
        stateModel.properties().forEach( propertyModel -> {
            Object initialValue = propertyModel.resolveInitialValue(module);
            if( propertyModel.accessor().equals( HasIdentity.IDENTITY_METHOD ) )
            {
                initialValue = identity;
            }

            Property<?> property = new PropertyInstance<>( propertyModel, initialValue );
            properties.put( propertyModel.accessor(), property );
        } );

        TransientStateInstance state = new TransientStateInstance( properties );
        ServiceInstance compositeInstance = new ServiceInstance( this, mixins, state );

        // Instantiate all mixins
        int i = 0;
        UsesInstance uses = UsesInstance.EMPTY_USES.use( this );
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        return compositeInstance;
    }

    @Override
    public String toString()
    {
        return super.toString() + ":" + identity;
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    public Class calculateConfigurationType()
    {
        DependencyModel.ScopeSpecification thisSpec = new DependencyModel.ScopeSpecification( This.class );
        Predicate<DependencyModel> configurationCheck = item -> item.rawInjectionType().equals( Configuration.class );
        return dependencies().filter( thisSpec.and( configurationCheck ) )
            .filter( dependencyModel -> dependencyModel.rawInjectionType().equals( Configuration.class ) )
            .filter( dependencyModel -> dependencyModel.injectionType() instanceof ParameterizedType )
            .map( dependencyModel -> Classes.RAW_CLASS.apply( ( (ParameterizedType) dependencyModel.injectionType() ).getActualTypeArguments()[ 0 ] ) )
            .reduce( null, ( injectionClass, type ) -> {
                if( injectionClass == null )
                {
                    injectionClass = type;
                }
                else
                {
                    if( injectionClass.isAssignableFrom( type ) )
                    {
                        injectionClass = type;
                    }
                }
                return injectionClass;
            } );
    }
}
