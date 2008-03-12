/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.AssociationField;
import org.qi4j.composite.scope.AssociationParameter;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.composite.scope.Invocation;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.composite.scope.PropertyParameter;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.SideEffectFor;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.composite.scope.Uses;
import org.qi4j.runtime.composite.AbstractCompositeInstance;
import org.qi4j.runtime.composite.CompositeBinder;
import org.qi4j.runtime.composite.CompositeModelFactory;
import org.qi4j.runtime.composite.CompositeResolver;
import org.qi4j.runtime.composite.InjectionProviderFactoryStrategy;
import org.qi4j.runtime.composite.InstanceFactory;
import org.qi4j.runtime.composite.InstanceFactoryImpl;
import org.qi4j.runtime.composite.ObjectBinder;
import org.qi4j.runtime.composite.ObjectModelFactory;
import org.qi4j.runtime.composite.ObjectResolver;
import org.qi4j.runtime.injection.AssociationInjectionProviderFactory;
import org.qi4j.runtime.injection.InvocationInjectionProviderFactory;
import org.qi4j.runtime.injection.ModifiesInjectionProviderFactory;
import org.qi4j.runtime.injection.PropertyInjectionProviderFactory;
import org.qi4j.runtime.injection.ServiceInjectionProviderFactory;
import org.qi4j.runtime.injection.StructureInjectionProviderFactory;
import org.qi4j.runtime.injection.ThisCompositeAsInjectionProviderFactory;
import org.qi4j.runtime.injection.UsesInjectionProviderFactory;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.injection.InjectionProviderFactory;

/**
 * Incarnation of Qi4j.
 */
public final class Energy4Java
    implements Qi4jRuntime
{
    private InstanceFactory instanceFactory;
    private CompositeModelFactory compositeModelFactory;
    private CompositeResolver compositeResolver;
    private CompositeBinder compositeBinder;

    private ObjectModelFactory objectModelFactory;
    private ObjectResolver objectResolver;
    private ObjectBinder objectBinder;

    public Energy4Java()
    {
        this( null );
    }

    public Energy4Java( Qi4jRuntime delegate )
    {
        Map<Class<? extends Annotation>, InjectionProviderFactory> providerFactories = new HashMap<Class<? extends Annotation>, InjectionProviderFactory>();
        providerFactories.put( ThisCompositeAs.class, new ThisCompositeAsInjectionProviderFactory() );
        ModifiesInjectionProviderFactory modifiesInjectionProviderFactory = new ModifiesInjectionProviderFactory();
        providerFactories.put( ConcernFor.class, modifiesInjectionProviderFactory );
        providerFactories.put( SideEffectFor.class, modifiesInjectionProviderFactory );
        providerFactories.put( Invocation.class, new InvocationInjectionProviderFactory() );
        providerFactories.put( Uses.class, new UsesInjectionProviderFactory() );
        PropertyInjectionProviderFactory propertyInjectionProviderFactory = new PropertyInjectionProviderFactory();
        providerFactories.put( PropertyField.class, propertyInjectionProviderFactory );
        providerFactories.put( PropertyParameter.class, propertyInjectionProviderFactory );
        AssociationInjectionProviderFactory associationInjectionProviderFactory = new AssociationInjectionProviderFactory();
        providerFactories.put( AssociationField.class, associationInjectionProviderFactory );
        providerFactories.put( AssociationParameter.class, associationInjectionProviderFactory );
        providerFactories.put( Structure.class, new StructureInjectionProviderFactory( this ) );
        providerFactories.put( Service.class, new ServiceInjectionProviderFactory() );
        InjectionProviderFactory ipf = new InjectionProviderFactoryStrategy( providerFactories );

        if( delegate != null )
        {
            instanceFactory = delegate.getInstanceFactory();
            compositeModelFactory = delegate.getCompositeModelFactory();
            compositeResolver = delegate.getCompositeResolver();
            compositeBinder = delegate.getCompositeBinder();
            objectModelFactory = delegate.getObjectModelFactory();
            objectResolver = delegate.getObjectResolver();
            objectBinder = delegate.getObjectBinder();
        }
        if( instanceFactory == null )
        {
            instanceFactory = new InstanceFactoryImpl();
        }
        if( compositeModelFactory == null )
        {
            compositeModelFactory = new CompositeModelFactory();
        }
        if( compositeResolver == null )
        {
            compositeResolver = new CompositeResolver();
        }
        if( compositeBinder == null )
        {
            compositeBinder = new CompositeBinder( ipf );
        }
        if( objectModelFactory == null )
        {
            objectModelFactory = new ObjectModelFactory();
        }
        if( objectResolver == null )
        {
            objectResolver = new ObjectResolver();
        }
        if( objectBinder == null )
        {
            objectBinder = new ObjectBinder( ipf );
        }
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
        return AbstractCompositeInstance.getCompositeInstance( composite ).getContext().getCompositeBinding();
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
