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

package org.qi4j.runtime.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.CompositeDeclaration;
import org.qi4j.bootstrap.EntityDeclaration;
import org.qi4j.bootstrap.InfoDeclaration;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.MetaInfoDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ObjectDeclaration;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.structure.CompositesModel;
import org.qi4j.runtime.structure.EntitiesModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.ObjectsModel;
import org.qi4j.runtime.structure.ServicesModel;
import org.qi4j.service.DuplicateServiceIdentityException;
import org.qi4j.service.ServiceComposite;
import org.qi4j.service.ServiceInstanceFactory;
import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * Assembly of a Module. This is where you register all objects, Composites,
 * Services. Each "add" method returns a declaration that you can use to add
 * additional information and metadata. If you call an "add" method with many
 * parameters then the declared metadata will apply to all types in the method
 * call.
 */
public final class ModuleAssemblyImpl
    implements ModuleAssembly
{
    private LayerAssembly layerAssembly;
    private String name;
    private final List<CompositeDeclarationImpl> compositeDeclarations = new ArrayList<CompositeDeclarationImpl>();
    private final List<EntityDeclarationImpl> entityDeclarations = new ArrayList<EntityDeclarationImpl>();
    private final List<ObjectDeclarationImpl> objectDeclarations = new ArrayList<ObjectDeclarationImpl>();
    private final List<ServiceDeclarationImpl> serviceDeclarations = new ArrayList<ServiceDeclarationImpl>();
    private final MetaInfoDeclaration metaInfoDeclaration = new MetaInfoDeclaration();

    public ModuleAssemblyImpl( LayerAssembly layerAssembly, String name )
    {
        this.layerAssembly = layerAssembly;
        this.name = name;
    }

    public void addAssembler( Assembler assembler )
        throws AssemblyException
    {
        // Invoke Assembler callback
        assembler.assemble( this );
    }

    public LayerAssembly layerAssembly()
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

        CompositeDeclarationImpl compositeDeclaration = new CompositeDeclarationImpl( compositeTypes );
        compositeDeclarations.add( compositeDeclaration );
        return compositeDeclaration;
    }

    public EntityDeclaration addEntities( Class<? extends EntityComposite>... compositeTypes )
        throws AssemblyException
    {
        EntityDeclarationImpl entityDeclaration = new EntityDeclarationImpl( compositeTypes );
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

        ObjectDeclarationImpl objectDeclaration = new ObjectDeclarationImpl( Arrays.asList( objectTypes ) );
        objectDeclarations.add( objectDeclaration );
        return objectDeclaration;
    }

    public ServiceDeclaration addServices( Class<? extends ServiceComposite>... serviceTypes )
        throws AssemblyException
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

        List<Class<? extends ServiceComposite>> classes = Arrays.asList( serviceTypes );
        ServiceDeclarationImpl serviceDeclaration = new ServiceDeclarationImpl( classes, this );
        serviceDeclarations.add( serviceDeclaration );
        return serviceDeclaration;
    }

    public <T> InfoDeclaration<T> on( Class<T> mixinType )
    {
        return metaInfoDeclaration.on( mixinType );
    }

    ModuleModel assembleModule()
        throws AssemblyException
    {
        List<CompositeModel> compositeModels = new ArrayList<CompositeModel>();
        List<EntityModel> entityModels = new ArrayList<EntityModel>();
        List<ObjectModel> objectModels = new ArrayList<ObjectModel>();
        List<ServiceModel> serviceModels = new ArrayList<ServiceModel>();

        if( name == null )
        {
            throw new AssemblyException( "Module must have name set" );
        }

        ModuleModel moduleModel = new ModuleModel( name,
                                                   new CompositesModel( compositeModels ),
                                                   new EntitiesModel( entityModels ),
                                                   new ObjectsModel( objectModels ),
                                                   new ServicesModel( serviceModels ) );

        for( CompositeDeclarationImpl compositeDeclaration : compositeDeclarations )
        {
            compositeDeclaration.addComposites( compositeModels, metaInfoDeclaration );
        }

        for( EntityDeclarationImpl entityDeclaration : entityDeclarations )
        {
            entityDeclaration.addEntities( entityModels, metaInfoDeclaration, metaInfoDeclaration );
        }

        for( ObjectDeclarationImpl objectDeclaration : objectDeclarations )
        {
            objectDeclaration.addObjects( objectModels );
        }

        for( ServiceDeclarationImpl serviceDeclaration : serviceDeclarations )
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
                    Class<? extends Composite> serviceModelType = serviceModel.type();
                    CompositeModel compositeModel = CompositeModel.newModel( serviceModelType, Visibility.module, new MetaInfo(), metaInfoDeclaration );
                    compositeModels.add( compositeModel );
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
                Class<? extends ServiceInstanceFactory> serviceFactoryType = serviceModel.serviceFactory();
                ObjectModel objectModel = new ObjectModel( serviceFactoryType, Visibility.module, new MetaInfo() );
                objectModels.add( objectModel );
            }
        }

        return moduleModel;
    }
}
