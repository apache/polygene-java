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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.injection.scope.This;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.composite.DefaultCompositeInstance;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.DependencyVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.service.Configuration;
import org.qi4j.service.ServiceComposite;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.structure.Module;

/**
 * Incarnation of Qi4j.
 */
public final class Energy4Java
    implements Qi4jRuntime
{
    public Energy4Java()
    {
    }

    // API
    public Composite dereference( Composite composite )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( composite );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (Composite) ( (ProxyReferenceInvocationHandler) handler ).proxy();
        }
        if( handler instanceof CompositeInstance )
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
        CompositeModel descriptor = (CompositeModel) getCompositeDescriptor( serviceComposite );
        final List<DependencyModel> dependencyModels = new ArrayList<DependencyModel>();
        descriptor.visitDependencies( new DependencyVisitor()
        {
            public void visit( DependencyModel dependencyModel, Resolution resolution )
            {
                if( dependencyModel.injectionAnnotation().annotationType().equals( This.class ) )
                {
                    dependencyModels.add( dependencyModel );
                }
            }
        } );

        for( DependencyModel dependencyModel : dependencyModels )
        {
            if( dependencyModel.rawInjectionType().equals( Configuration.class ) )
            {
                return dependencyModel.injectionClass();
            }
        }

        return null;
    }

    // SPI
    public CompositeDescriptor getCompositeDescriptor( Composite composite )
    {
        return DefaultCompositeInstance.getCompositeInstance( composite ).compositeModel();
    }

    public CompositeDescriptor getCompositeDescriptor( Class mixinType, Module module )
    {
        return ( (ModuleInstance) module ).findCompositeFor( mixinType );
    }

    public void setMixins( Composite composite, Object[] mixins )
    {
        DefaultCompositeInstance instance = DefaultCompositeInstance.getCompositeInstance( composite );

        instance.setMixins( mixins );
    }
}
