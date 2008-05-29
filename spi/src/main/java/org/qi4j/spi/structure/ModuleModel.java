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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public final class ModuleModel
    implements Serializable
{
    private String name;

    private Iterable<CompositeDescriptor> compositeDescriptors;
    private Iterable<ObjectDescriptor> objectDescriptors;
    private Iterable<ServiceDescriptor> serviceDescriptors;
    private Map<Method, PropertyDescriptor> propertyDescriptors;
    private Map<Method, AssociationDescriptor> associationDescriptors;
    private Map<Visibility, Map<String, Class>> classMappings;

    public ModuleModel( String name, Iterable<CompositeDescriptor> compositeDescriptors,
                        Iterable<ObjectDescriptor> objectDescriptors,
                        Iterable<ServiceDescriptor> serviceDescriptors,
                        Map<Method, PropertyDescriptor> propertyDescriptors,
                        Map<Method, AssociationDescriptor> associationDescriptors )
    {
        this.name = name;
        this.compositeDescriptors = compositeDescriptors;
        this.objectDescriptors = objectDescriptors;
        this.serviceDescriptors = serviceDescriptors;
        this.propertyDescriptors = propertyDescriptors;
        this.associationDescriptors = associationDescriptors;

        // Mapping for string->class lookups
        classMappings = new HashMap<Visibility, Map<String, Class>>();
        classMappings.put( Visibility.module, new HashMap<String, Class>() );
        classMappings.put( Visibility.layer, new HashMap<String, Class>() );
        classMappings.put( Visibility.application, new HashMap<String, Class>() );

        for( CompositeDescriptor compositeDescriptor : compositeDescriptors )
        {
            classMappings.get( compositeDescriptor.getVisibility() )
                .put( compositeDescriptor.getCompositeModel().getCompositeType().getName(), compositeDescriptor.getCompositeModel().getCompositeType() );
        }

        for( ObjectDescriptor objectDescriptor : objectDescriptors )
        {
            classMappings.get( objectDescriptor.getVisibility() )
                .put( objectDescriptor.getObjectModel().getModelClass().getName(), objectDescriptor.getObjectModel().getModelClass() );
        }
    }

    public String getName()
    {
        return name;
    }

    public Iterable<CompositeDescriptor> compositeDescriptors()
    {
        return compositeDescriptors;
    }

    public Iterable<ObjectDescriptor> objectDescriptors()
    {
        return objectDescriptors;
    }

    public Iterable<ServiceDescriptor> serviceDescriptors()
    {
        return serviceDescriptors;
    }

    public PropertyDescriptor getPropertyDescriptor( Method propertyMethod )
    {
        return propertyDescriptors.get( propertyMethod );
    }

    public AssociationDescriptor getAssociationDescriptor( Method associationMethod )
    {
        return associationDescriptors.get( associationMethod );
    }

    public Class getClass( String name, Visibility visibility )
    {
        return classMappings.get( visibility ).get( name );
    }

    @Override public String toString()
    {
        return name;
    }

    public String toURI()
    {
        return "urn:qi4j:model:module:" + name;
    }
}
