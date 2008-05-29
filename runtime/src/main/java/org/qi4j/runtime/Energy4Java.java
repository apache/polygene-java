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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.AssociationField;
import org.qi4j.composite.scope.AssociationParameter;
import org.qi4j.composite.scope.Invocation;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.composite.scope.PropertyParameter;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.This;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.EntityComposite;
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
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.injection.AssociationInjectionProviderFactory;
import org.qi4j.runtime.injection.InvocationInjectionProviderFactory;
import org.qi4j.runtime.injection.ModifiesInjectionProviderFactory;
import org.qi4j.runtime.injection.PropertyInjectionProviderFactory;
import org.qi4j.runtime.injection.ServiceInjectionProviderFactory;
import org.qi4j.runtime.injection.StructureInjectionProviderFactory;
import org.qi4j.runtime.injection.UsesInjectionProviderFactory;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.service.Configuration;
import org.qi4j.service.ServiceComposite;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.injection.InjectionModel;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.structure.Module;

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
        // providerFactories.put( This.class, new ThisInjectionProviderFactory() );
        ModifiesInjectionProviderFactory modifiesInjectionProviderFactory = new ModifiesInjectionProviderFactory();
        //providerFactories.put( ConcernFor.class, modifiesInjectionProviderFactory );
        //providerFactories.put( SideEffectFor.class, modifiesInjectionProviderFactory );
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

        processDelegate( delegate );

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
    public Composite dereference( Composite composite )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( composite );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (Composite) ( (ProxyReferenceInvocationHandler) handler ).proxy();
        }
        if( handler instanceof AbstractCompositeInstance )
        {
            return composite;
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public <S extends Composite, T extends S> Class<S> getSuperComposite( Class<T> compositeClass )
    {
        Class<?>[] extendedInterfaces = compositeClass.getInterfaces();
        for( Class<?> extendedInterface : extendedInterfaces )
        {
            if( Composite.class.isAssignableFrom( extendedInterface ) &&
                !Composite.class.equals( extendedInterface ) &&
                !EntityComposite.class.equals( extendedInterface ) &&
                !ServiceComposite.class.equals( extendedInterface )
                )
            {
                return (Class<S>) extendedInterface;
            }
        }
        return null; // No super Composite type found
    }

    public Class<?> getConfigurationType( Composite serviceComposite )
    {
        CompositeBinding binding = getCompositeBinding( serviceComposite );
        Iterable<InjectionModel> injections = binding.getCompositeResolution().getCompositeModel().getInjectionsByScope( This.class );
        for( InjectionModel injection : injections )
        {
            if( injection.getRawInjectionType().equals( Configuration.class ) )
            {
                return (Class<?>) ( (ParameterizedType) injection.getInjectionType() ).getActualTypeArguments()[ 0 ];
            }
        }

        return null;
    }

    // SPI
    public CompositeBinding getCompositeBinding( Composite composite )
    {
        return AbstractCompositeInstance.getCompositeInstance( composite ).getContext().getCompositeBinding();
    }

    public CompositeBinding getCompositeBinding( Class<? extends Composite> compositeType, Module module )
    {
        ModuleInstance.ModuleDelegate delegate = (ModuleInstance.ModuleDelegate) module;
        return delegate.getModuleInstance().findCompositeBinding( compositeType );
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

    private void processDelegate( Qi4jRuntime delegate )
    {
        if( delegate != null )
        {
            // Alternate method to do it, to ensure we don't forget methods if they are added
            // to the Qi4jRuntime interface.
            // If there is ever a bug that the delegate has been forgotten, strong suggestion that this
            // code is introduced instead of the simple assignments now in place below.
//            Method[] methods = Qi4jRuntime.class.getDeclaredMethods();
//            try
//            {
//            for( Method method : methods )
//            {
//                String name = method.getName();
//                if( name.startsWith( "get" ) )
//                {
//                    name = name.substring( 3 );
//                    name = "" + Character.toLowerCase( name.charAt( 0 ) ) + name.substring( 1 );
//                    Field f = getClass().getField( name );
//                    f.setAccessible( true );
//                    f.set( this, method.invoke( delegate ) );
//                }
//            }
//            }
//            catch( Exception e )
//            {
//                InternalError error = new InternalError( "Energy4Java is corrupt." );
//                error.initCause( e );
//                throw error;
//            }
            instanceFactory = delegate.getInstanceFactory();
            compositeModelFactory = delegate.getCompositeModelFactory();
            compositeResolver = delegate.getCompositeResolver();
            compositeBinder = delegate.getCompositeBinder();
            objectModelFactory = delegate.getObjectModelFactory();
            objectResolver = delegate.getObjectResolver();
            objectBinder = delegate.getObjectBinder();
        }
    }
}
