/*  Copyright 2007 Rickard …berg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.api.ObjectBuilder;
import org.qi4j.api.PropertyValue;
import org.qi4j.api.model.Binding;
import org.qi4j.api.model.InjectionKey;
import org.qi4j.runtime.resolution.ObjectResolution;
import org.qi4j.spi.dependency.DependencyInjectionContext;
import org.qi4j.spi.dependency.ObjectDependencyInjectionContext;

/**
 *
 */
public class ObjectBuilderImpl<T>
    implements ObjectBuilder<T>
{
    private ObjectResolution<T> objectResolution;
    private InstanceFactory instanceFactory;

    private Map<InjectionKey, Object> adaptContext;
    private Map<InjectionKey, Object> decorateContext;
    private Map<InjectionKey, Object> propertyContext;


    ObjectBuilderImpl( ObjectResolution<T> objectResolution, InstanceFactory instanceFactory )
    {
        this.objectResolution = objectResolution;
        this.instanceFactory = instanceFactory;
    }

    public void adapt( Object adaptedObject )
    {
        if( adaptedObject instanceof Binding )
        {
            Binding binding = (Binding) adaptedObject;
            getAdaptContext().put( binding.getKey(), binding.getValue() );
        }
        else
        {
            InjectionKey key = new InjectionKey( adaptedObject.getClass(), null, objectResolution.getObjectModel().getModelClass() );
            getAdaptContext().put( key, adaptedObject );
        }
    }

    public void decorate( Object decoratedObject )
    {
        if( decoratedObject instanceof Binding )
        {
            Binding binding = (Binding) decoratedObject;
            getDecorateContext().put( binding.getKey(), binding.getValue() );
        }
        else
        {
            InjectionKey key = new InjectionKey( decoratedObject.getClass(), null, objectResolution.getObjectModel().getModelClass() );
            getDecorateContext().put( key, decoratedObject );
        }
    }

    public void properties( PropertyValue... properties )
    {
        Map<InjectionKey, Object> context = getPropertyContext();
        for( PropertyValue property : properties )
        {
            String name = property.getName();
            Object value = property.getValue();
            InjectionKey key = new InjectionKey( value.getClass(), name, objectResolution.getObjectModel().getModelClass() );
            context.put( key, value );
        }
    }

    public T newInstance()
    {
        // Instantiate object
        Map<InjectionKey, Object> adapt = adaptContext == null ? Collections.EMPTY_MAP : adaptContext;
        Map<InjectionKey, Object> decorate = decorateContext == null ? Collections.EMPTY_MAP : decorateContext;
        Map<InjectionKey, Object> props = propertyContext == null ? Collections.EMPTY_MAP : propertyContext;

        DependencyInjectionContext context = new ObjectDependencyInjectionContext( props, adapt, decorate );
        T instance = instanceFactory.newInstance( objectResolution, context );
        return instance;
    }

    public void inject( T instance )
    {
        // Inject existing object
        Map<InjectionKey, Object> adapt = adaptContext == null ? Collections.EMPTY_MAP : adaptContext;
        Map<InjectionKey, Object> decorate = decorateContext == null ? Collections.EMPTY_MAP : decorateContext;
        Map<InjectionKey, Object> props = propertyContext == null ? Collections.EMPTY_MAP : propertyContext;

        DependencyInjectionContext context = new ObjectDependencyInjectionContext( props, adapt, decorate );
        instanceFactory.inject( instance, objectResolution, context );
    }

    // Private ------------------------------------------------------
    private Map<InjectionKey, Object> getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new LinkedHashMap<InjectionKey, Object>();
        }
        return adaptContext;
    }

    private Map<InjectionKey, Object> getDecorateContext()
    {
        if( decorateContext == null )
        {
            decorateContext = new LinkedHashMap<InjectionKey, Object>();
        }
        return decorateContext;
    }

    private Map<InjectionKey, Object> getPropertyContext()
    {
        if( propertyContext == null )
        {
            propertyContext = new LinkedHashMap<InjectionKey, Object>();
        }
        return propertyContext;
    }
}