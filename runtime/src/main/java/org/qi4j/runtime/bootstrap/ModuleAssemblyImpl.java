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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.service.DuplicateServiceIdentityException;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.AssemblyVisitor;
import org.qi4j.bootstrap.EntityDeclaration;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.MetaInfoDeclaration;
import org.qi4j.bootstrap.MixinDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ObjectDeclaration;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.bootstrap.TransientDeclaration;
import org.qi4j.bootstrap.ValueDeclaration;
import org.qi4j.runtime.composite.CompositesModel;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.object.ObjectsModel;
import org.qi4j.runtime.service.ImportedServiceModel;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.structure.EntitiesModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.runtime.value.ValuesModel;

/**
 * Assembly of a Module. This is where you register all objects, Composites,
 * Services. Each "add" method returns a declaration that you can use to add
 * additional information and metadata. If you call an "add" method with many
 * parameters then the declared metadata will apply to all types in the method
 * call.
 */
public final class ModuleAssemblyImpl
    implements ModuleAssembly, Serializable
{
    private LayerAssembly layerAssembly;
    private String name;
    private MetaInfo metaInfo = new MetaInfo();
    private final List<TransientDeclarationImpl> compositeDeclarations = new ArrayList<TransientDeclarationImpl>();
    private final List<EntityDeclarationImpl> entityDeclarations = new ArrayList<EntityDeclarationImpl>();
    private final List<ValueDeclarationImpl> valueDeclarations = new ArrayList<ValueDeclarationImpl>();
    private final List<ObjectDeclarationImpl> objectDeclarations = new ArrayList<ObjectDeclarationImpl>();
    private final List<ServiceDeclarationImpl> serviceDeclarations = new ArrayList<ServiceDeclarationImpl>();
    private final List<ImportedServiceDeclarationImpl> importedServiceDeclarations = new ArrayList<ImportedServiceDeclarationImpl>();
    private final MetaInfoDeclaration metaInfoDeclaration = new MetaInfoDeclaration();

    public ModuleAssemblyImpl( LayerAssembly layerAssembly, String name )
    {
        this.layerAssembly = layerAssembly;
        this.name = name;
    }

    public LayerAssembly layerAssembly()
    {
        return layerAssembly;
    }

    public ModuleAssembly setName( String name )
    {
        this.name = name;
        return this;
    }

    public String name()
    {
        return name;
    }

    public ModuleAssembly setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public ValueDeclaration addValues( Class<? extends ValueComposite>... compositeTypes )
    {
        ValueDeclarationImpl valueDeclaration = new ValueDeclarationImpl( compositeTypes );
        valueDeclarations.add( valueDeclaration );
        return valueDeclaration;
    }

    public TransientDeclaration addTransients( Class<? extends TransientComposite>... compositeTypes )
        throws AssemblyException
    {
        for( Class<? extends TransientComposite> compositeType : compositeTypes )
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
            else if( ValueComposite.class.isAssignableFrom( compositeType ) )
            {
                throw new AssemblyException( "May not register ValueComposites as a Composite:" + compositeType.getName() );
            }
        }

        TransientDeclarationImpl compositeDeclaration = new TransientDeclarationImpl( compositeTypes );
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

    public ImportedServiceDeclaration importServices( Class... serviceTypes )
        throws AssemblyException
    {
        List<Class> classes = Arrays.asList( serviceTypes );
        ImportedServiceDeclarationImpl serviceDeclaration = new ImportedServiceDeclarationImpl( classes, this );
        importedServiceDeclarations.add( serviceDeclaration );
        return serviceDeclaration;
    }

    public <T> MixinDeclaration<T> forMixin( Class<T> mixinType )
    {
        return metaInfoDeclaration.on( mixinType );
    }

    public void visit( AssemblyVisitor visitor )
        throws AssemblyException
    {
        visitor.visitModule( this );

        for( TransientDeclarationImpl compositeDeclaration : compositeDeclarations )
        {
            visitor.visitComposite( compositeDeclaration );
        }

        for( EntityDeclarationImpl entityDeclaration : entityDeclarations )
        {
            visitor.visitEntity( entityDeclaration );
        }

        for( ObjectDeclarationImpl objectDeclaration : objectDeclarations )
        {
            visitor.visitObject( objectDeclaration );
        }

        for( ServiceDeclarationImpl serviceDeclaration : serviceDeclarations )
        {
            visitor.visitService( serviceDeclaration );
        }

        for( ImportedServiceDeclarationImpl importedServiceDeclaration : importedServiceDeclarations )
        {
            visitor.visitImportedService( importedServiceDeclaration );
        }

        for( ValueDeclarationImpl valueDeclaration : valueDeclarations )
        {
            visitor.visitValue( valueDeclaration );
        }
    }

    ModuleModel assembleModule()
        throws AssemblyException
    {
        List<TransientModel> transientModels = new ArrayList<TransientModel>();
        List<EntityModel> entityModels = new ArrayList<EntityModel>();
        List<ObjectModel> objectModels = new ArrayList<ObjectModel>();
        List<ValueModel> valueModels = new ArrayList<ValueModel>();
        List<ServiceModel> serviceModels = new ArrayList<ServiceModel>();
        List<ImportedServiceModel> importedServiceModels = new ArrayList<ImportedServiceModel>();

        if( name == null )
        {
            throw new AssemblyException( "Module must have name set" );
        }

        ModuleModel moduleModel = new ModuleModel( name,
                                                   metaInfo, new CompositesModel( transientModels ),
                                                   new EntitiesModel( entityModels ),
                                                   new ObjectsModel( objectModels ),
                                                   new ValuesModel( valueModels ),
                                                   new ServicesModel( serviceModels ),
                                                   new ImportedServicesModel( importedServiceModels ) );

        for( TransientDeclarationImpl compositeDeclaration : compositeDeclarations )
        {
            compositeDeclaration.addComposites( transientModels, metaInfoDeclaration );
        }

        for( ValueDeclarationImpl valueDeclaration : valueDeclarations )
        {
            valueDeclaration.addValues( valueModels, metaInfoDeclaration );
        }

        for( EntityDeclarationImpl entityDeclaration : entityDeclarations )
        {
            entityDeclaration.addEntities( entityModels, metaInfoDeclaration, metaInfoDeclaration, metaInfoDeclaration );
        }

        for( ObjectDeclarationImpl objectDeclaration : objectDeclarations )
        {
            objectDeclaration.addObjects( objectModels );
        }

        for( ServiceDeclarationImpl serviceDeclaration : serviceDeclarations )
        {
            serviceDeclaration.addServices( serviceModels );
        }

        for( ImportedServiceDeclarationImpl importedServiceDeclaration : importedServiceDeclarations )
        {
            importedServiceDeclaration.addServices( importedServiceModels );
        }

        // Check for duplicate service identities
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
        for( ImportedServiceModel serviceModel : importedServiceModels )
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

        for( ImportedServiceModel importedServiceModel : importedServiceModels )
        {
            boolean found = false;
            for( ObjectModel objectModel : objectModels )
            {
                if( objectModel.type().equals( importedServiceModel.serviceImporter() ) )
                {
                    found = true;
                    break;
                }
            }
            if( !found )
            {
                Class<? extends ServiceImporter> serviceFactoryType = importedServiceModel.serviceImporter();
                ObjectModel objectModel = new ObjectModel( serviceFactoryType, Visibility.module, new MetaInfo() );
                objectModels.add( objectModel );
            }
        }

        return moduleModel;
    }
}
