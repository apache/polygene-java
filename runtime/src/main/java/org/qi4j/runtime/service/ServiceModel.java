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

package org.qi4j.runtime.service;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specifications;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Specifications.and;
import static org.qi4j.functional.Specifications.translate;

/**
 * JAVADOC
 */
public final class ServiceModel
        extends CompositeModel
        implements ServiceDescriptor
{
    private static Method identityMethod;
    static
    {
        try
        {
            identityMethod = Identity.class.getMethod( "identity" );
        } catch( NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }

    private final String identity;
    private final boolean instantiateOnStartup;
    private final Class configurationType;

    public ServiceModel( Class<?> compositeType,
                         Iterable<Class<?>> types,
                         Visibility visibility,
                         MetaInfo metaInfo,
                         MixinsModel mixinsModel,
                         StateModel stateModel,
                         CompositeMethodsModel compositeMethodsModel,
                         String identity,
                         boolean instantiateOnStartup
    )
    {
        super( compositeType, types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

        this.identity = identity;
        this.instantiateOnStartup = instantiateOnStartup;

        // Calculate configuration type
        this.configurationType = calculateConfigurationType();
    }

    public boolean isInstantiateOnStartup()
    {
        return instantiateOnStartup;
    }

    public String identity()
    {
        return identity;
    }

    public <T> Class<T> configurationType()
    {
        return configurationType;
    }

    public ServiceInstance newInstance( final ModuleInstance module )
    {
        Object[] mixins = mixinsModel.newMixinHolder();

        Map<AccessibleObject, Property<?>> properties = new HashMap<AccessibleObject, Property<?>>();
        for( PropertyModel propertyModel : stateModel.properties() )
        {
            Object initialValue = propertyModel.initialValue( module );
            if (propertyModel.accessor().equals(identityMethod))
                initialValue = identity;

            Property property = new PropertyInstance<Object>(propertyModel, initialValue );
            properties.put( propertyModel.accessor(), property );
        }

        TransientStateInstance state = new TransientStateInstance( properties );
        ServiceInstance compositeInstance = new ServiceInstance( this, module, mixins, state );

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
        return type().getName() + ":" + identity;
    }

    public Class calculateConfigurationType()
    {
        Class injectionClass = null;
        Iterable<DependencyModel> configurationThisDependencies = filter( and( translate( new DependencyModel.InjectionTypeFunction(), Specifications.<Class<?>>in( Configuration.class ) ), new DependencyModel.ScopeSpecification( This.class ) ), dependencies() );
        for( DependencyModel dependencyModel : configurationThisDependencies )
        {
            if( dependencyModel.rawInjectionType().equals( Configuration.class ) && dependencyModel.injectionType() instanceof ParameterizedType )
            {
                Class<?> type = Classes.RAW_CLASS.map(((ParameterizedType)dependencyModel.injectionType()).getActualTypeArguments()[0]);
                if( injectionClass == null )
                {
                    injectionClass = type;
                } else
                {
                    if( injectionClass.isAssignableFrom( type ) )
                    {
                        injectionClass = type;
                    }
                }
            }
        }
        return injectionClass;
    }

    public void activate( Object[] mixins )
            throws Exception
    {
        mixinsModel.activate( mixins );
    }

    public void passivate( Object[] mixins )
            throws Exception
    {
        mixinsModel.passivate( mixins );
    }
}
