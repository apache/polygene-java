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

package org.qi4j.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.spi.service.ServiceProvider;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public final class ModuleAssembly
{
    private LayerAssembly layerAssembly;
    private Map<Class, ServiceProvider> serviceProviders;
    private String name;
    private List<CompositeDeclaration> compositeDeclarations;
    private List<ObjectDeclaration> objectDeclarations;
    private List<PropertyDeclaration> propertyDeclarations;
    private List<AssociationDeclaration> associationDeclarations;

    public ModuleAssembly( LayerAssembly layerAssembly )
    {
        this.layerAssembly = layerAssembly;
        compositeDeclarations = new ArrayList<CompositeDeclaration>();
        objectDeclarations = new ArrayList<ObjectDeclaration>();
        propertyDeclarations = new ArrayList<PropertyDeclaration>();
        associationDeclarations = new ArrayList<AssociationDeclaration>();
        serviceProviders = new HashMap<Class, ServiceProvider>();
    }

    public void addAssembly( Assembly assembly )
        throws AssemblyException
    {
        // Invoke Assembly callbacks
        assembly.configure( this );
    }

    public LayerAssembly getLayerAssembly()
    {
        return layerAssembly;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public CompositeDeclaration addComposites( Class<? extends Composite>... compositeTypes )
    {
        CompositeDeclaration compositeDeclaration = new CompositeDeclaration( compositeTypes );
        compositeDeclarations.add( compositeDeclaration );
        return compositeDeclaration;
    }

    public ObjectDeclaration addObjects( Class... objectTypes )
    {
        ObjectDeclaration objectDeclaration = new ObjectDeclaration( Arrays.asList( objectTypes ) );
        objectDeclarations.add( objectDeclaration );
        return objectDeclaration;
    }

    public void addServiceProvider( ServiceProvider serviceProvider, Class... serviceTypes )
    {
        for( Class serviceType : serviceTypes )
        {
            serviceProviders.put( serviceType, serviceProvider );

            if( Composite.class.isAssignableFrom( serviceType ) )
            {
                addComposites( (Class<? extends Composite>) serviceType );
            }
        }

        addObjects( serviceProvider.getClass() );
    }

    public PropertyDeclaration addProperty()
    {
        PropertyDeclaration declaration = new PropertyDeclaration();
        propertyDeclarations.add( declaration );
        return declaration;
    }

    public AssociationDeclaration addAssociation()
    {
        AssociationDeclaration declaration = new AssociationDeclaration();
        associationDeclarations.add( declaration );
        return declaration;
    }

    List<CompositeDeclaration> getCompositeDeclarations()
    {
        return compositeDeclarations;
    }

    List<ObjectDeclaration> getObjectDeclarations()
    {
        return objectDeclarations;
    }

    Set<Class<? extends Composite>> getPublicComposites()
    {
        Set<Class<? extends Composite>> publicComposites = new HashSet<Class<? extends Composite>>();
        for( CompositeDeclaration compositeDeclaration : compositeDeclarations )
        {
            if( compositeDeclaration.getVisibility() == Visibility.module )
            {
                for( Class<? extends Composite> compositeType : compositeDeclaration.getCompositeTypes() )
                {
                    publicComposites.add( compositeType );
                }
            }
        }
        return publicComposites;
    }

    Set<Class<? extends Composite>> getPrivateComposites()
    {
        Set<Class<? extends Composite>> privateComposites = new HashSet<Class<? extends Composite>>();
        for( CompositeDeclaration compositeDeclaration : compositeDeclarations )
        {
            if( compositeDeclaration.getVisibility() == Visibility.none )
            {
                for( Class<? extends Composite> compositeType : compositeDeclaration.getCompositeTypes() )
                {
                    privateComposites.add( compositeType );
                }
            }
        }
        return privateComposites;
    }

    Set<Class<?>> getPublicObjects()
    {
        Set<Class<?>> publicObjects = new HashSet<Class<?>>();
        for( ObjectDeclaration objectDeclaration : objectDeclarations )
        {
            if( objectDeclaration.getVisibility() == Visibility.module )
            {
                for( Class<? extends Composite> objectType : objectDeclaration.getObjectClasses() )
                {
                    publicObjects.add( objectType );
                }
            }
        }
        return publicObjects;
    }

    Set<Class<?>> getPrivateObjects()
    {
        Set<Class<?>> publicObjects = new HashSet<Class<?>>();
        for( ObjectDeclaration objectDeclaration : objectDeclarations )
        {
            if( objectDeclaration.getVisibility() == Visibility.none )
            {
                for( Class<? extends Composite> objectType : objectDeclaration.getObjectClasses() )
                {
                    publicObjects.add( objectType );
                }
            }
        }
        return publicObjects;
    }

    Map<Class, ServiceProvider> getServiceProviders()
    {
        return serviceProviders;
    }

    List<PropertyDeclaration> getPropertyDeclarations()
    {
        return propertyDeclarations;
    }

    List<AssociationDeclaration> getAssociationDeclarations()
    {
        return associationDeclarations;
    }

    String getName()
    {
        return name;
    }
}
