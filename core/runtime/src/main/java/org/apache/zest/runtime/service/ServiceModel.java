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
package org.apache.zest.runtime.service;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.identity.HasIdentity;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.util.Classes;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.runtime.activation.ActivatorsInstance;
import org.apache.zest.runtime.activation.ActivatorsModel;
import org.apache.zest.runtime.composite.CompositeMethodsModel;
import org.apache.zest.runtime.composite.CompositeModel;
import org.apache.zest.runtime.composite.MixinModel;
import org.apache.zest.runtime.composite.MixinsModel;
import org.apache.zest.runtime.composite.StateModel;
import org.apache.zest.runtime.composite.TransientStateInstance;
import org.apache.zest.runtime.composite.UsesInstance;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.property.PropertyInstance;

import static org.apache.zest.runtime.legacy.Specifications.translate;

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
            Object initialValue = propertyModel.initialValue( module );
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
        Predicate<DependencyModel> configurationCheck =
            translate( new DependencyModel.InjectionTypeFunction(), clazz -> clazz.equals( Configuration.class ) );
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
