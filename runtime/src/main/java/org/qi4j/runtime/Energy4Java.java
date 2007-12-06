/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.annotation.scope.Adapt;
import org.qi4j.annotation.scope.ConcernFor;
import org.qi4j.annotation.scope.Decorate;
import org.qi4j.annotation.scope.Invocation;
import org.qi4j.annotation.scope.PropertyField;
import org.qi4j.annotation.scope.PropertyParameter;
import org.qi4j.annotation.scope.Service;
import org.qi4j.annotation.scope.SideEffectFor;
import org.qi4j.annotation.scope.Structure;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.composite.CompositeBinder;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.runtime.composite.CompositeResolver;
import org.qi4j.runtime.composite.InjectionProviderFactoryStrategy;
import org.qi4j.runtime.composite.ObjectBinder;
import org.qi4j.runtime.composite.ObjectResolver;
import org.qi4j.runtime.injection.AdaptInjectionProviderFactory;
import org.qi4j.runtime.injection.DecorateInjectionProviderFactory;
import org.qi4j.runtime.injection.InvocationInjectionProviderFactory;
import org.qi4j.runtime.injection.ModifiesInjectionProviderFactory;
import org.qi4j.runtime.injection.PropertyInjectionProviderFactory;
import org.qi4j.runtime.injection.ServiceInjectionProviderFactory;
import org.qi4j.runtime.injection.StructureInjectionProviderFactory;
import org.qi4j.runtime.injection.ThisCompositeAsInjectionProviderFactory;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.injection.InjectionProviderFactory;

/**
 * Incarnation of Qi4j.
 */
public class Energy4Java
    implements Qi4jRuntime
{
    Qi4jRuntime delegate;

    InstanceFactory instanceFactory;
    CompositeModelFactory compositeModelFactory;
    CompositeResolver compositeResolver;
    CompositeBinder compositeBinder;

    ObjectModelFactory objectModelFactory;
    ObjectResolver objectResolver;
    ObjectBinder objectBinder;

    public Energy4Java()
    {
        this( null );
    }

    public Energy4Java( Qi4jRuntime delegate )
    {
        this.delegate = delegate;

        Map<Class<? extends Annotation>, InjectionProviderFactory> providerFactories = new HashMap<Class<? extends Annotation>, InjectionProviderFactory>();
        providerFactories.put( ThisCompositeAs.class, new ThisCompositeAsInjectionProviderFactory() );
        ModifiesInjectionProviderFactory modifiesInjectionProviderFactory = new ModifiesInjectionProviderFactory();
        providerFactories.put( ConcernFor.class, modifiesInjectionProviderFactory );
        providerFactories.put( SideEffectFor.class, modifiesInjectionProviderFactory );
        providerFactories.put( Invocation.class, new InvocationInjectionProviderFactory() );
        providerFactories.put( Adapt.class, new AdaptInjectionProviderFactory() );
        providerFactories.put( Decorate.class, new DecorateInjectionProviderFactory() );
        PropertyInjectionProviderFactory dependencyResolver = new PropertyInjectionProviderFactory();
        providerFactories.put( PropertyField.class, dependencyResolver );
        providerFactories.put( PropertyParameter.class, dependencyResolver );
        providerFactories.put( Structure.class, new StructureInjectionProviderFactory( this ) );
        providerFactories.put( Service.class, new ServiceInjectionProviderFactory() );
        InjectionProviderFactory ipf = new InjectionProviderFactoryStrategy( providerFactories );

        instanceFactory = delegate == null ? new InstanceFactoryImpl() : delegate.getInstanceFactory();
        compositeModelFactory = delegate == null ? new CompositeModelFactory() : delegate.getCompositeModelFactory();
        compositeResolver = delegate == null ? new CompositeResolver() : delegate.getCompositeResolver();
        compositeBinder = delegate == null ? new CompositeBinder( ipf ) : delegate.getCompositeBinder();

        objectModelFactory = delegate == null ? new ObjectModelFactory() : delegate.getObjectModelFactory();
        objectResolver = delegate == null ? new ObjectResolver() : delegate.getObjectResolver();
        objectBinder = delegate == null ? new ObjectBinder( ipf ) : delegate.getObjectBinder();
    }

    // API

    public <S extends Composite, T extends S> Class<S> getSuperComposite( Class<T> compositeClass )
    {
        Class[] extendedInterfaces = compositeClass.getInterfaces();
        for( Class extendedInterface : extendedInterfaces )
        {
            if( Composite.class.isAssignableFrom( extendedInterface ) && !Composite.class.equals( extendedInterface ) )
            {
                return extendedInterface;
            }
        }
        return null; // No super Composite type found
    }

    // SPI
    public CompositeBinding getCompositeBinding( Composite composite )
    {
        return CompositeInstance.getCompositeInstance( composite ).getContext().getCompositeBinding();
    }

    // Runtime
    public InstanceFactory getInstanceFactory()
    {
        return instanceFactory;
    }

    public CompositeModelFactory getCompositeModelFactory()
    {
        return compositeModelFactory;
    }

    public CompositeResolver getCompositeResolver()
    {
        return compositeResolver;
    }

    public CompositeBinder getCompositeBinder()
    {
        return compositeBinder;
    }

    public ObjectModelFactory getObjectModelFactory()
    {
        return objectModelFactory;
    }

    public ObjectResolver getObjectResolver()
    {
        return objectResolver;
    }

    public ObjectBinder getObjectBinder()
    {
        return objectBinder;
    }
}
