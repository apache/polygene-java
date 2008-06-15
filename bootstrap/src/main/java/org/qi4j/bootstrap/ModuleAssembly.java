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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.service.qi.ServiceModel;
import org.qi4j.runtime.structure.CompositesModel;
import org.qi4j.runtime.structure.EntitiesModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.ObjectsModel;
import org.qi4j.runtime.structure.ServicesModel;
import org.qi4j.service.DuplicateServiceIdentityException;
import org.qi4j.service.ServiceComposite;
import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * Assembly of a Module. This is where you register all objects, Composites,
 * Services. Each "add" method returns a declaration that you can use to add
 * additional information and metadata. If you call an "add" method with many
 * parameters then the declared metadata will apply to all types in the method
 * call.
 */
public final class ModuleAssembly
{
    private LayerAssembly layerAssembly;
    private String name;
    private List<CompositeDeclaration> compositeDeclarations;
    private List<EntityDeclaration> entityDeclarations;
    private List<ObjectDeclaration> objectDeclarations;
    private List<ServiceDeclaration> serviceDeclarations;
    private List<PropertyDeclaration> propertyDeclarations;
    private List<AssociationDeclaration> associationDeclarations;

    public ModuleAssembly( LayerAssembly layerAssembly )
    {
        this.layerAssembly = layerAssembly;
        compositeDeclarations = new ArrayList<CompositeDeclaration>();
        entityDeclarations = new ArrayList<EntityDeclaration>();
        objectDeclarations = new ArrayList<ObjectDeclaration>();
        serviceDeclarations = new ArrayList<ServiceDeclaration>();
        propertyDeclarations = new ArrayList<PropertyDeclaration>();
        associationDeclarations = new ArrayList<AssociationDeclaration>();
    }

    public void addAssembler( Assembler assembler )
        throws AssemblyException
    {
        // Invoke Assembler callback
        assembler.assemble( this );
    }

    public LayerAssembly getLayerAssembly()
    {
        return layerAssembly;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String name()
    {
        return name;
    }

    public CompositeDeclaration addComposites( Class<? extends Composite>... compositeTypes )
        throws AssemblyException
    {
        for( Class<? extends Composite> compositeType : compositeTypes )
        {
            // May not register ServiceComposites
            if( ServiceComposite.class.isAssignableFrom( compositeType ) )
            {
                throw new AssemblyException( "May not register ServiceComposites as a Composite:" + compositeType.getName() );
            }
            else if( EntityComposite.class.isAssignableFrom( compositeType ) )
            {
                throw new AssemblyException( "May not register EntityComposites as a Composite:" + compositeType.getName() );
            }
        }

        CompositeDeclaration compositeDeclaration = new CompositeDeclaration( compositeTypes );
        compositeDeclarations.add( compositeDeclaration );
        return compositeDeclaration;
    }

    public EntityDeclaration addEntities( Class<? extends EntityComposite>... compositeTypes )
        throws AssemblyException
    {
        EntityDeclaration entityDeclaration = new EntityDeclaration( compositeTypes );
        entityDeclarations.add( entityDeclaration );
        return entityDeclaration;
    }

    public ObjectDeclaration addObjects( Class... objectTypes )
        throws AssemblyException
    {
        for( Class objectType : objectTypes )
        {
            if( objectType.isPrimitive() )
            {
                throw new AssemblyException( "May not register primitives as objects" );
            }
            if( objectType.isInterface() )
            {
                throw new AssemblyException( "May not register interfaces as objects" );
            }
        }

        ObjectDeclaration objectDeclaration = new ObjectDeclaration( Arrays.asList( objectTypes ) );
        objectDeclarations.add( objectDeclaration );
        return objectDeclaration;
    }

    public ServiceDeclaration addServices( Class... serviceTypes ) throws AssemblyException
    {
        for( Class serviceType : serviceTypes )
        {
            if( !serviceType.isInterface() )
            {
                throw new AssemblyException( "May not register classes as service types" );
            }

            if( Composite.class.isAssignableFrom( serviceType ) && !ServiceComposite.class.isAssignableFrom( serviceType ) )
            {
                throw new AssemblyException( "May not register Composites which are not ServiceComposites" );
            }
        }

        ServiceDeclaration serviceDeclaration = new ServiceDeclaration( Arrays.asList( serviceTypes ) );
        serviceDeclarations.add( serviceDeclaration );
        return serviceDeclaration;
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

    ModuleModel assembleModule( LayerModel layerModel )
    {
        List<CompositeModel> compositeModels = new ArrayList<CompositeModel>();
        List<EntityModel> entityModels = new ArrayList<EntityModel>();
        List<ObjectModel> objectModels = new ArrayList<ObjectModel>();
        List<ServiceModel> serviceModels = new ArrayList<ServiceModel>();
        ModuleModel moduleModel = new ModuleModel( name,
                                                   layerModel,
                                                   new CompositesModel( compositeModels ),
                                                   new EntitiesModel( entityModels ),
                                                   new ObjectsModel( objectModels ),
                                                   new ServicesModel( serviceModels ) );

        for( CompositeDeclaration compositeDeclaration : compositeDeclarations )
        {
            compositeDeclaration.addComposites( compositeModels );
        }

        for( EntityDeclaration entityDeclaration : entityDeclarations )
        {
            entityDeclaration.addEntities( entityModels );
        }

        for( ObjectDeclaration objectDeclaration : objectDeclarations )
        {
            objectDeclaration.addObjects( objectModels );
        }

        for( ServiceDeclaration serviceDeclaration : serviceDeclarations )
        {
            serviceDeclaration.addServices( serviceModels );
        }

        Set<String> identities = new HashSet<String>();
        for( ServiceModel serviceModel : serviceModels )
        {
            String identity = serviceModel.identity();
            if( identities.contains( identity ) )
            {
                throw new DuplicateServiceIdentityException(
                    "Duplicated service identity: " + identity + " in module " + moduleModel.name()
                );
            }
            identities.add( identity );
        }

        nextService:
        for( ServiceModel serviceModel : serviceModels )
        {
            if( Composite.class.isAssignableFrom( serviceModel.type() ) )
            {
                boolean found = false;
                for( CompositeModel compositeModel : compositeModels )
                {
                    if( serviceModel.type().isAssignableFrom( compositeModel.type() ) )
                    {
                        found = true;
                        break;
                    }
                }

                // Auto-add implementation as Composite with Module visibility.
                if( !found )
                {
                    compositeModels.add(
                        CompositeModel.newModel( serviceModel.type(), Visibility.module, new MetaInfo() )
                    );
                }
            }

            boolean found = false;
            for( ObjectModel objectModel : objectModels )
            {
                if( objectModel.type().equals( serviceModel.serviceFactory() ) )
                {
                    found = true;
                    break;
                }
            }
            if( !found )
            {
                objectModels.add(
                    new ObjectModel( serviceModel.serviceFactory(), Visibility.module, new MetaInfo() )
                );
            }
        }

        return moduleModel;
    }
}
