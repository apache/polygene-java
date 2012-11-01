/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.service.DuplicateServiceIdentityException;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.AssemblySpecifications;
import org.qi4j.bootstrap.AssemblyVisitor;
import org.qi4j.bootstrap.EntityAssembly;
import org.qi4j.bootstrap.EntityDeclaration;
import org.qi4j.bootstrap.ImportedServiceAssembly;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.MetaInfoDeclaration;
import org.qi4j.bootstrap.MixinDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ObjectAssembly;
import org.qi4j.bootstrap.ObjectDeclaration;
import org.qi4j.bootstrap.ServiceAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.bootstrap.TransientAssembly;
import org.qi4j.bootstrap.TransientDeclaration;
import org.qi4j.bootstrap.ValueAssembly;
import org.qi4j.bootstrap.ValueDeclaration;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.runtime.activation.ActivatorsModel;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.composite.TransientsModel;
import org.qi4j.runtime.entity.EntitiesModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.object.ObjectsModel;
import org.qi4j.runtime.service.ImportedServiceModel;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.runtime.value.ValuesModel;

import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.iterable;

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
    private MetaInfo metaInfo = new MetaInfo();
    private List<Class<? extends Activator<Module>>> activators = new ArrayList<Class<? extends Activator<Module>>>();

    private final List<ServiceAssemblyImpl> serviceAssemblies = new ArrayList<ServiceAssemblyImpl>();
    private final Map<Class<?>, ImportedServiceAssemblyImpl> importedServiceAssemblies = new LinkedHashMap<Class<?>, ImportedServiceAssemblyImpl>();
    private final Map<Class<? extends EntityComposite>, EntityAssemblyImpl> entityAssemblies = new LinkedHashMap<Class<? extends EntityComposite>, EntityAssemblyImpl>();
    private final Map<Class<? extends ValueComposite>, ValueAssemblyImpl> valueAssemblies = new LinkedHashMap<Class<? extends ValueComposite>, ValueAssemblyImpl>();
    private final Map<Class<? extends TransientComposite>, TransientAssemblyImpl> transientAssemblies = new LinkedHashMap<Class<? extends TransientComposite>, TransientAssemblyImpl>();
    private final Map<Class<?>, ObjectAssemblyImpl> objectAssemblies = new LinkedHashMap<Class<?>, ObjectAssemblyImpl>();

    private final MetaInfoDeclaration metaInfoDeclaration = new MetaInfoDeclaration();

    public ModuleAssemblyImpl( LayerAssembly layerAssembly, String name )
    {
        this.layerAssembly = layerAssembly;
        this.name = name;
    }

    @Override
    public LayerAssembly layer()
    {
        return layerAssembly;
    }

    @Override
    public ModuleAssembly setName( String name )
    {
        this.name = name;
        return this;
    }

    @Override
    public String name()
    {
        return name;
    }

    public ModuleAssembly setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    @Override
    public ModuleAssembly withActivators( Class<? extends Activator<Module>>... activators )
    {
        this.activators.addAll( Arrays.asList( activators ) );
        return this;
    }

    @Override
    public ValueDeclaration values( Class<?>... valueTypes )
    {
        List<ValueAssemblyImpl> assemblies = new ArrayList<ValueAssemblyImpl>();

        for( Class valueType : valueTypes )
        {
            if( valueAssemblies.containsKey( valueType ) )
            {
                assemblies.add( valueAssemblies.get( valueType ) );
            }
            else
            {
                ValueAssemblyImpl valueAssembly = new ValueAssemblyImpl( valueType );
                valueAssemblies.put( valueType, valueAssembly );
                assemblies.add( valueAssembly );
            }
        }

        return new ValueDeclarationImpl( assemblies );
    }

    @Override
    public ValueDeclaration values( Specification<? super ValueAssembly> specification )
    {
        List<ValueAssemblyImpl> assemblies = new ArrayList<ValueAssemblyImpl>();
        for( ValueAssemblyImpl transientAssembly : valueAssemblies.values() )
        {
            if( specification.satisfiedBy( transientAssembly ) )
            {
                assemblies.add( transientAssembly );
            }
        }

        return new ValueDeclarationImpl( assemblies );
    }

    @Override
    public TransientDeclaration transients( Class<?>... transientTypes )
    {
        List<TransientAssemblyImpl> assemblies = new ArrayList<TransientAssemblyImpl>();

        for( Class valueType : transientTypes )
        {
            if( transientAssemblies.containsKey( valueType ) )
            {
                assemblies.add( transientAssemblies.get( valueType ) );
            }
            else
            {
                TransientAssemblyImpl transientAssembly = new TransientAssemblyImpl( valueType );
                transientAssemblies.put( valueType, transientAssembly );
                assemblies.add( transientAssembly );
            }
        }

        return new TransientDeclarationImpl( assemblies );
    }

    @Override
    public TransientDeclaration transients( Specification<? super TransientAssembly> specification )
    {
        List<TransientAssemblyImpl> assemblies = new ArrayList<TransientAssemblyImpl>();
        for( TransientAssemblyImpl transientAssembly : transientAssemblies.values() )
        {
            if( specification.satisfiedBy( transientAssembly ) )
            {
                assemblies.add( transientAssembly );
            }
        }

        return new TransientDeclarationImpl( assemblies );
    }

    @Override
    public EntityDeclaration entities( Class<?>... entityTypes )
    {
        List<EntityAssemblyImpl> assemblies = new ArrayList<EntityAssemblyImpl>();

        for( Class entityType : entityTypes )
        {
            if( entityAssemblies.containsKey( entityType ) )
            {
                assemblies.add( entityAssemblies.get( entityType ) );
            }
            else
            {
                EntityAssemblyImpl entityAssembly = new EntityAssemblyImpl( entityType );
                entityAssemblies.put( entityType, entityAssembly );
                assemblies.add( entityAssembly );
            }
        }

        return new EntityDeclarationImpl( assemblies );
    }

    @Override
    public EntityDeclaration entities( Specification<? super EntityAssembly> specification )
    {
        List<EntityAssemblyImpl> assemblies = new ArrayList<EntityAssemblyImpl>();
        for( EntityAssemblyImpl entityAssembly : entityAssemblies.values() )
        {
            if( specification.satisfiedBy( entityAssembly ) )
            {
                assemblies.add( entityAssembly );
            }
        }

        return new EntityDeclarationImpl( assemblies );
    }

    @Override
    public ObjectDeclaration objects( Class<?>... objectTypes )
        throws AssemblyException
    {
        List<ObjectAssemblyImpl> assemblies = new ArrayList<ObjectAssemblyImpl>();

        for( Class<?> objectType : objectTypes )
        {
            if( objectType.isInterface() )
            {
                throw new AssemblyException( "Interfaces can not be Qi4j Objects." );
            }
            if( objectAssemblies.containsKey( objectType ) )
            {
                assemblies.add( objectAssemblies.get( objectType ) );
            }
            else
            {
                ObjectAssemblyImpl objectAssembly = new ObjectAssemblyImpl( objectType );
                objectAssemblies.put( objectType, objectAssembly );
                assemblies.add( objectAssembly );
            }
        }

        return new ObjectDeclarationImpl( assemblies );
    }

    @Override
    public ObjectDeclaration objects( Specification<? super ObjectAssembly> specification )
    {
        List<ObjectAssemblyImpl> assemblies = new ArrayList<ObjectAssemblyImpl>();
        for( ObjectAssemblyImpl objectAssembly : objectAssemblies.values() )
        {
            if( specification.satisfiedBy( objectAssembly ) )
            {
                assemblies.add( objectAssembly );
            }
        }

        return new ObjectDeclarationImpl( assemblies );
    }

    @Override
    public ServiceDeclaration addServices( Class<?>... serviceTypes )
    {
        List<ServiceAssemblyImpl> assemblies = new ArrayList<ServiceAssemblyImpl>();

        for( Class<?> serviceType : serviceTypes )
        {
            ServiceAssemblyImpl serviceAssembly = new ServiceAssemblyImpl( serviceType );
            serviceAssemblies.add( serviceAssembly );
            assemblies.add( serviceAssembly );
        }

        return new ServiceDeclarationImpl( assemblies );
    }

    @Override
    public ServiceDeclaration services( Class<?>... serviceTypes )
    {
        List<ServiceAssemblyImpl> assemblies = new ArrayList<ServiceAssemblyImpl>();

        for( Class<?> serviceType : serviceTypes )
        {
            if( Iterables.matchesAny( AssemblySpecifications.types( serviceType ), serviceAssemblies ) )
            {
                Iterables.addAll( assemblies, Iterables.filter( AssemblySpecifications.types( serviceType ), serviceAssemblies ) );
            }
            else
            {
                ServiceAssemblyImpl serviceAssembly = new ServiceAssemblyImpl( serviceType );
                serviceAssemblies.add( serviceAssembly );
                assemblies.add( serviceAssembly );
            }
        }

        return new ServiceDeclarationImpl( assemblies );
    }

    @Override
    public ServiceDeclaration services( Specification<? super ServiceAssembly> specification )
    {
        List<ServiceAssemblyImpl> assemblies = new ArrayList<ServiceAssemblyImpl>();
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            if( specification.satisfiedBy( serviceAssembly ) )
            {
                assemblies.add( serviceAssembly );
            }
        }

        return new ServiceDeclarationImpl( assemblies );
    }

    @Override
    public ImportedServiceDeclaration importedServices( Class<?>... serviceTypes )
    {
        List<ImportedServiceAssemblyImpl> assemblies = new ArrayList<ImportedServiceAssemblyImpl>();

        for( Class<?> serviceType : serviceTypes )
        {
            if( importedServiceAssemblies.containsKey( serviceType ) )
            {
                assemblies.add( importedServiceAssemblies.get( serviceType ) );
            }
            else
            {
                ImportedServiceAssemblyImpl serviceAssembly = new ImportedServiceAssemblyImpl( serviceType, this );
                importedServiceAssemblies.put( serviceType, serviceAssembly );
                assemblies.add( serviceAssembly );
            }
        }

        return new ImportedServiceDeclarationImpl( assemblies );
    }

    @Override
    public ImportedServiceDeclaration importedServices( Specification<? super ImportedServiceAssembly> specification )
    {
        List<ImportedServiceAssemblyImpl> assemblies = new ArrayList<ImportedServiceAssemblyImpl>();
        for( ImportedServiceAssemblyImpl objectAssembly : importedServiceAssemblies.values() )
        {
            if( specification.satisfiedBy( objectAssembly ) )
            {
                assemblies.add( objectAssembly );
            }
        }

        return new ImportedServiceDeclarationImpl( assemblies );
    }

    @Override
    public <T> MixinDeclaration<T> forMixin( Class<T> mixinType )
    {
        return metaInfoDeclaration.on( mixinType );
    }

    @Override
    public <ThrowableType extends Throwable> void visit( AssemblyVisitor<ThrowableType> visitor )
        throws ThrowableType
    {
        visitor.visitModule( this );

        for( TransientAssemblyImpl compositeDeclaration : transientAssemblies.values() )
        {
            visitor.visitComposite( new TransientDeclarationImpl( iterable( compositeDeclaration ) ) );
        }

        for( EntityAssemblyImpl entityDeclaration : entityAssemblies.values() )
        {
            visitor.visitEntity( new EntityDeclarationImpl( iterable( entityDeclaration ) ) );
        }

        for( ObjectAssemblyImpl objectDeclaration : objectAssemblies.values() )
        {
            visitor.visitObject( new ObjectDeclarationImpl( iterable( objectDeclaration ) ) );
        }

        for( ServiceAssemblyImpl serviceDeclaration : serviceAssemblies )
        {
            visitor.visitService( new ServiceDeclarationImpl( iterable( serviceDeclaration ) ) );
        }

        for( ImportedServiceAssemblyImpl importedServiceDeclaration : importedServiceAssemblies.values() )
        {
            visitor.visitImportedService( new ImportedServiceDeclarationImpl( iterable( importedServiceDeclaration ) ) );
        }

        for( ValueAssemblyImpl valueDeclaration : valueAssemblies.values() )
        {
            visitor.visitValue( new ValueDeclarationImpl( iterable( valueDeclaration ) ) );
        }
    }

    ModuleModel assembleModule( AssemblyHelper helper )
        throws AssemblyException
    {
        List<TransientModel> transientModels = new ArrayList<TransientModel>();
        List<ObjectModel> objectModels = new ArrayList<ObjectModel>();
        List<ValueModel> valueModels = new ArrayList<ValueModel>();
        List<ServiceModel> serviceModels = new ArrayList<ServiceModel>();
        List<ImportedServiceModel> importedServiceModels = new ArrayList<ImportedServiceModel>();

        if( name == null )
        {
            throw new AssemblyException( "Module must have name set" );
        }

        for( TransientAssemblyImpl compositeDeclaration : transientAssemblies.values() )
        {
            transientModels.add( compositeDeclaration.newTransientModel( metaInfoDeclaration, helper ) );
        }

        for( ValueAssemblyImpl valueDeclaration : valueAssemblies.values() )
        {
            valueModels.add( valueDeclaration.newValueModel( metaInfoDeclaration, helper ) );
        }

        List<EntityModel> entityModels = new ArrayList<EntityModel>();
        for( EntityAssemblyImpl entityDeclaration : entityAssemblies.values() )
        {
            entityModels.add( entityDeclaration.newEntityModel( metaInfoDeclaration, metaInfoDeclaration, metaInfoDeclaration, helper ) );
        }

        for( ObjectAssemblyImpl objectDeclaration : objectAssemblies.values() )
        {
            objectDeclaration.addObjectModel( objectModels );
        }

        for( ServiceAssemblyImpl serviceDeclaration : serviceAssemblies )
        {
            if( serviceDeclaration.identity == null )
            {
                serviceDeclaration.identity = generateId( serviceDeclaration.types() );
            }

            serviceModels.add( serviceDeclaration.newServiceModel( metaInfoDeclaration, helper ) );
        }

        for( ImportedServiceAssemblyImpl importedServiceDeclaration : importedServiceAssemblies.values() )
        {
            importedServiceDeclaration.addImportedServiceModel( importedServiceModels );
        }

        ModuleModel moduleModel = new ModuleModel( name,
                                                   metaInfo,
                                                   new ActivatorsModel<Module>( activators ),
                                                   new TransientsModel( transientModels ),
                                                   new EntitiesModel( entityModels ),
                                                   new ObjectsModel( objectModels ),
                                                   new ValuesModel( valueModels ),
                                                   new ServicesModel( serviceModels ),
                                                   new ImportedServicesModel( importedServiceModels ) );

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
                if( first( objectModel.types() ).equals( importedServiceModel.serviceImporter() ) )
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

    private String generateId( Iterable<Class<?>> serviceTypes )
    {
        // Find service identity that is not yet used
        Class<?> serviceType = serviceTypes.iterator()
            .next(); // Use the first Iterable, which *SHOULD* be the main serviceType
        int idx = 0;
        String id = serviceType.getSimpleName();
        boolean invalid;
        do
        {
            invalid = false;
            for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
            {
                if( serviceAssembly.identity() != null && serviceAssembly.identity().equals( id ) )
                {
                    idx++;
                    id = serviceType.getSimpleName() + "_" + idx;
                    invalid = true;
                    break;
                }
            }
        }
        while( invalid );
        return id;
    }
}
