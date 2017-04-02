/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.bootstrap;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.activation.Activator;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.service.DuplicateServiceIdentityException;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.type.HasEqualOrAssignableFromType;
import org.apache.polygene.api.type.HasTypes;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.AssemblySpecifications;
import org.apache.polygene.bootstrap.AssemblyVisitor;
import org.apache.polygene.bootstrap.ConfigurationDeclaration;
import org.apache.polygene.bootstrap.EntityAssembly;
import org.apache.polygene.bootstrap.EntityDeclaration;
import org.apache.polygene.bootstrap.ImportedServiceAssembly;
import org.apache.polygene.bootstrap.ImportedServiceDeclaration;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.MetaInfoDeclaration;
import org.apache.polygene.bootstrap.MixinDeclaration;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.ObjectAssembly;
import org.apache.polygene.bootstrap.ObjectDeclaration;
import org.apache.polygene.bootstrap.ServiceAssembly;
import org.apache.polygene.bootstrap.ServiceDeclaration;
import org.apache.polygene.bootstrap.TransientAssembly;
import org.apache.polygene.bootstrap.TransientDeclaration;
import org.apache.polygene.bootstrap.ValueAssembly;
import org.apache.polygene.bootstrap.ValueDeclaration;
import org.apache.polygene.bootstrap.identity.DefaultIdentityGeneratorAssembler;
import org.apache.polygene.bootstrap.serialization.DefaultSerializationAssembler;
import org.apache.polygene.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.polygene.runtime.activation.ActivatorsModel;
import org.apache.polygene.runtime.composite.TransientModel;
import org.apache.polygene.runtime.composite.TransientsModel;
import org.apache.polygene.runtime.entity.EntitiesModel;
import org.apache.polygene.runtime.entity.EntityModel;
import org.apache.polygene.runtime.object.ObjectModel;
import org.apache.polygene.runtime.object.ObjectsModel;
import org.apache.polygene.runtime.service.ImportedServiceModel;
import org.apache.polygene.runtime.service.ImportedServicesModel;
import org.apache.polygene.runtime.service.ServiceModel;
import org.apache.polygene.runtime.service.ServicesModel;
import org.apache.polygene.runtime.structure.LayerModel;
import org.apache.polygene.runtime.structure.ModuleModel;
import org.apache.polygene.runtime.value.ValueModel;
import org.apache.polygene.runtime.value.ValuesModel;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

/**
 * Assembly of a Module. This is where you register all objects, Composites,
 * Services. Each "add" method returns a declaration that you can use to add
 * additional information and metadata. If you call an "add" method with many
 * parameters then the declared metadata will apply to all types in the method
 * call.
 */
final class ModuleAssemblyImpl
        implements ModuleAssembly
{
    private static HashMap<Class, Assembler> defaultAssemblers;

    private final LayerAssembly layerAssembly;
    private String name;
    private final MetaInfo metaInfo = new MetaInfo();

    private final List<Class<? extends Activator<Module>>> activators = new ArrayList<>();
    private final List<ServiceAssemblyImpl> serviceAssemblies = new ArrayList<>();
    private final Map<Class<?>, ImportedServiceAssemblyImpl> importedServiceAssemblies = new LinkedHashMap<>();
    private final Map<Class<? extends EntityComposite>, EntityAssemblyImpl> entityAssemblies = new LinkedHashMap<>();
    private final Map<Class<? extends ValueComposite>, ValueAssemblyImpl> valueAssemblies = new LinkedHashMap<>();
    private final Map<Class<? extends TransientComposite>, TransientAssemblyImpl> transientAssemblies = new LinkedHashMap<>();

    private final Map<Class<?>, ObjectAssemblyImpl> objectAssemblies = new LinkedHashMap<>();

    private final MetaInfoDeclaration metaInfoDeclaration = new MetaInfoDeclaration();

    static
    {
        defaultAssemblers = new HashMap<>();
        defaultAssemblers.put( UnitOfWorkFactory.class, new DefaultUnitOfWorkAssembler() );
        defaultAssemblers.put( IdentityGenerator.class, new DefaultIdentityGeneratorAssembler() );
        defaultAssemblers.put( Serialization.class, new DefaultSerializationAssembler() );
    }

    ModuleAssemblyImpl(LayerAssembly layerAssembly, String name)
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
    public ModuleAssembly module(String layerName, String moduleName)
    {
        return layerAssembly.application().module(layerName, moduleName);
    }

    @Override
    public ModuleAssembly setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public String name()
    {
        return name;
    }

    public ModuleAssembly setMetaInfo(Object info)
    {
        metaInfo.set(info);
        return this;
    }

    @Override
    @SafeVarargs
    public final ModuleAssembly withActivators(Class<? extends Activator<Module>>... activators)
    {
        this.activators.addAll( asList( activators ) );
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValueDeclaration values(Class<?>... valueTypes)
    {
        List<ValueAssemblyImpl> assemblies = new ArrayList<>();

        for (Class valueType : valueTypes)
        {
            if (valueAssemblies.containsKey(valueType))
            {
                assemblies.add(valueAssemblies.get(valueType));
            }
            else
            {
                ValueAssemblyImpl valueAssembly = new ValueAssemblyImpl(valueType);
                valueAssemblies.put(valueType, valueAssembly);
                assemblies.add(valueAssembly);
            }
        }

        return new ValueDeclarationImpl(assemblies);
    }

    @Override
    public ValueDeclaration values(Predicate<? super ValueAssembly> specification)
    {
        List<ValueAssemblyImpl> assemblies = valueAssemblies.values().stream()
                .filter(specification::test)
                .collect(toList());
        return new ValueDeclarationImpl(assemblies);
    }

    @Override
    @SuppressWarnings({"raw", "unchecked"})
    public TransientDeclaration transients(Class<?>... transientTypes)
    {
        List<TransientAssemblyImpl> assemblies = new ArrayList<>();

        for (Class valueType : transientTypes)
        {
            if (transientAssemblies.containsKey(valueType))
            {
                assemblies.add(transientAssemblies.get(valueType));
            }
            else
            {
                TransientAssemblyImpl transientAssembly = new TransientAssemblyImpl(valueType);
                transientAssemblies.put(valueType, transientAssembly);
                assemblies.add(transientAssembly);
            }
        }

        return new TransientDeclarationImpl(assemblies);
    }

    @Override
    public TransientDeclaration transients(Predicate<? super TransientAssembly> specification)
    {
        List<TransientAssemblyImpl> assemblies = transientAssemblies.values().stream()
                .filter(specification::test)
                .collect(toList());

        return new TransientDeclarationImpl(assemblies);
    }

    @Override
    @SuppressWarnings({"raw", "unchecked"})
    public EntityDeclaration entities(Class<?>... entityTypes)
    {
        List<EntityAssemblyImpl> assemblies = new ArrayList<>();

        for (Class entityType : entityTypes)
        {
            if (entityAssemblies.containsKey(entityType))
            {
                assemblies.add(entityAssemblies.get(entityType));
            }
            else
            {
                EntityAssemblyImpl entityAssembly = new EntityAssemblyImpl(entityType);
                entityAssemblies.put(entityType, entityAssembly);
                assemblies.add(entityAssembly);
            }
        }

        return new EntityDeclarationImpl(assemblies);
    }

    @Override
    public EntityDeclaration entities(Predicate<? super EntityAssembly> specification)
    {
        List<EntityAssemblyImpl> assemblies = entityAssemblies.values().stream()
                .filter(specification::test)
                .collect(toList());

        return new EntityDeclarationImpl(assemblies);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigurationDeclaration configurations(Class<?>... configurationTypes)
    {
        List<EntityAssemblyImpl> entityAssemblyList = new ArrayList<>();

        for (Class entityType : configurationTypes)
        {
            if (this.entityAssemblies.containsKey(entityType))
            {
                entityAssemblyList.add(this.entityAssemblies.get(entityType));
            }
            else
            {
                EntityAssemblyImpl entityAssembly = new EntityAssemblyImpl(entityType);
                this.entityAssemblies.put(entityType, entityAssembly);
                entityAssemblyList.add(entityAssembly);
            }
        }

        List<ValueAssemblyImpl> valueAssemblyList = new ArrayList<>();

        for (Class valueType : configurationTypes)
        {
            if (valueAssemblies.containsKey(valueType))
            {
                valueAssemblyList.add(valueAssemblies.get(valueType));
            }
            else
            {
                ValueAssemblyImpl valueAssembly = new ValueAssemblyImpl(valueType);
                valueAssemblies.put(valueType, valueAssembly);
                valueAssemblyList.add(valueAssembly);
                valueAssembly.types.add(HasIdentity.class);
            }
        }

        return new ConfigurationDeclarationImpl(entityAssemblyList, valueAssemblyList);
    }

    @Override
    public ConfigurationDeclaration configurations( Predicate<HasTypes> specification )
    {
        Predicate<HasTypes> isConfigurationComposite = new HasEqualOrAssignableFromType<>( HasIdentity.class );
        specification = specification.and( isConfigurationComposite );
        List<EntityAssemblyImpl> entityAssemblyList = new ArrayList<>();
        for( EntityAssemblyImpl entityAssembly : entityAssemblies.values() )
        {
            if( specification.test( entityAssembly ) )
            {
                entityAssemblyList.add( entityAssembly );
            }
        }
        List<ValueAssemblyImpl> valueAssemblyList = new ArrayList<>();
        for( ValueAssemblyImpl transientAssembly : valueAssemblies.values() )
        {
            if( specification.test( transientAssembly ) )
            {
                valueAssemblyList.add( transientAssembly );
            }
        }
        return new ConfigurationDeclarationImpl( entityAssemblyList, valueAssemblyList );
    }

    @Override
    public ObjectDeclaration objects(Class<?>... objectTypes)
            throws AssemblyException
    {
        List<ObjectAssemblyImpl> assemblies = new ArrayList<>();

        for (Class<?> objectType : objectTypes)
        {
            if (objectType.isInterface())
            {
                throw new AssemblyException("Interfaces can not be Polygene Objects.");
            }
            if (objectAssemblies.containsKey(objectType))
            {
                assemblies.add(objectAssemblies.get(objectType));
            }
            else
            {
                ObjectAssemblyImpl objectAssembly = new ObjectAssemblyImpl(objectType);
                objectAssemblies.put(objectType, objectAssembly);
                assemblies.add(objectAssembly);
            }
        }

        return new ObjectDeclarationImpl(assemblies);
    }

    @Override
    public ObjectDeclaration objects(Predicate<? super ObjectAssembly> specification)
    {
        List<ObjectAssemblyImpl> assemblies = objectAssemblies.values().stream()
                .filter(specification::test)
                .collect(toList());

        return new ObjectDeclarationImpl(assemblies);
    }

    @Override
    public ServiceDeclaration addServices(Class<?>... serviceTypes)
    {
        List<ServiceAssemblyImpl> assemblies = new ArrayList<>();

        for (Class<?> serviceType : serviceTypes)
        {
            ServiceAssemblyImpl serviceAssembly = new ServiceAssemblyImpl(serviceType);
            serviceAssemblies.add(serviceAssembly);
            assemblies.add(serviceAssembly);
        }

        return new ServiceDeclarationImpl(assemblies);
    }

    @Override
    public ServiceDeclaration services(Class<?>... serviceTypes)
    {
        List<ServiceAssemblyImpl> assemblies = new ArrayList<>();

        for (Class<?> serviceType : serviceTypes)
        {
            if( serviceAssemblies.stream().anyMatch( AssemblySpecifications.ofAnyType( serviceType ) ) )
            {
                serviceAssemblies.stream().filter( AssemblySpecifications.ofAnyType( serviceType ) )
                                 .forEach( assemblies::add );
            }
            else
            {
                ServiceAssemblyImpl serviceAssembly = new ServiceAssemblyImpl(serviceType);
                serviceAssemblies.add(serviceAssembly);
                assemblies.add(serviceAssembly);
            }
        }

        return new ServiceDeclarationImpl(assemblies);
    }

    @Override
    public ServiceDeclaration services(Predicate<? super ServiceAssembly> specification)
    {
        List<ServiceAssemblyImpl> assemblies = serviceAssemblies.stream()
                .filter(specification::test)
                .collect(toList());
        return new ServiceDeclarationImpl(assemblies);
    }

    @Override
    public ImportedServiceDeclaration importedServices(Class<?>... serviceTypes)
    {
        List<ImportedServiceAssemblyImpl> assemblies = new ArrayList<>();

        for (Class<?> serviceType : serviceTypes)
        {
            if (importedServiceAssemblies.containsKey(serviceType))
            {
                assemblies.add(importedServiceAssemblies.get(serviceType));
            }
            else
            {
                ImportedServiceAssemblyImpl serviceAssembly = new ImportedServiceAssemblyImpl(serviceType, this);
                importedServiceAssemblies.put(serviceType, serviceAssembly);
                assemblies.add(serviceAssembly);
            }
        }

        return new ImportedServiceDeclarationImpl(assemblies);
    }

    @Override
    public ImportedServiceDeclaration importedServices(Predicate<? super ImportedServiceAssembly> specification)
    {
        List<ImportedServiceAssemblyImpl> assemblies = importedServiceAssemblies.values().stream()
                .filter(specification::test)
                .collect(toList());

        return new ImportedServiceDeclarationImpl(assemblies);
    }

    @Override
    public <T> MixinDeclaration<T> forMixin(Class<T> mixinType)
    {
        return metaInfoDeclaration.on(mixinType);
    }

    @Override
    public <ThrowableType extends Throwable> void visit(AssemblyVisitor<ThrowableType> visitor)
            throws ThrowableType
    {
        visitor.visitModule( this );

        for( TransientAssemblyImpl compositeDeclaration : transientAssemblies.values() )
        {
            visitor.visitComposite( new TransientDeclarationImpl( singleton( compositeDeclaration ) ) );
        }

        for( EntityAssemblyImpl entityDeclaration : entityAssemblies.values() )
        {
            visitor.visitEntity( new EntityDeclarationImpl( singleton( entityDeclaration ) ) );
        }

        for( ObjectAssemblyImpl objectDeclaration : objectAssemblies.values() )
        {
            visitor.visitObject( new ObjectDeclarationImpl( singleton( objectDeclaration ) ) );
        }

        for( ServiceAssemblyImpl serviceDeclaration : serviceAssemblies )
        {
            visitor.visitService( new ServiceDeclarationImpl( singleton( serviceDeclaration ) ) );
        }

        for( ImportedServiceAssemblyImpl importedServiceDeclaration : importedServiceAssemblies.values() )
        {
            visitor.visitImportedService( new ImportedServiceDeclarationImpl( singleton( importedServiceDeclaration ) ) );
        }

        for( ValueAssemblyImpl valueDeclaration : valueAssemblies.values() )
        {
            visitor.visitValue( new ValueDeclarationImpl( singleton( valueDeclaration ) ) );
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ModuleModel assembleModule(LayerModel layerModel, AssemblyHelper helper)
            throws AssemblyException
    {
        addDefaultAssemblers();
        List<TransientModel> transientModels = new ArrayList<>();
        List<ObjectModel> objectModels = new ArrayList<>();
        List<ValueModel> valueModels = new ArrayList<>();
        List<ServiceModel> serviceModels = new ArrayList<>();
        List<ImportedServiceModel> importedServiceModels = new ArrayList<>();
        List<EntityModel> entityModels = new ArrayList<>();
        ModuleModel moduleModel = new ModuleModel(name,
                metaInfo,
                layerModel,
                new ActivatorsModel<>(activators),
                new TransientsModel(transientModels),
                new EntitiesModel(entityModels),
                new ObjectsModel(objectModels),
                new ValuesModel(valueModels),
                new ServicesModel(serviceModels),
                new ImportedServicesModel(importedServiceModels));

        if (name == null)
        {
            throw new AssemblyException("Module must have name set");
        }

        transientModels.addAll(transientAssemblies.values().stream()
                .map(composite -> composite.newTransientModel(moduleModel, metaInfoDeclaration, helper))
                .collect(toList()));

        valueModels.addAll(valueAssemblies.values().stream()
                .map(value -> value.newValueModel(moduleModel, metaInfoDeclaration, helper))
                .collect(toList()));

        entityModels.addAll(entityAssemblies.values().stream()
                .map(entityDeclaration -> entityDeclaration.newEntityModel(moduleModel,
                        metaInfoDeclaration,
                        metaInfoDeclaration,
                        metaInfoDeclaration,
                        metaInfoDeclaration,
                        helper))
                .collect(Collectors.toList()));

        for (ObjectAssemblyImpl objectDeclaration : objectAssemblies.values())
        {
            objectDeclaration.addObjectModel(moduleModel, objectModels);
        }

        for (ServiceAssemblyImpl serviceDeclaration : serviceAssemblies)
        {
            if (serviceDeclaration.identity == null)
            {
                serviceDeclaration.identity = generateId(serviceDeclaration.types());
            }

            serviceModels.add(serviceDeclaration.newServiceModel(moduleModel, metaInfoDeclaration, helper));
        }

        for (ImportedServiceAssemblyImpl importedServiceDeclaration : importedServiceAssemblies.values())
        {
            importedServiceDeclaration.addImportedServiceModel(moduleModel, importedServiceModels);
        }

        // Check for duplicate service identities
        Set<String> identities = new HashSet<>();
        for (ServiceModel serviceModel : serviceModels)
        {
            String identity = serviceModel.identity().toString();
            if (identities.contains(identity))
            {
                throw new DuplicateServiceIdentityException(
                        "Duplicated service reference: " + identity + " in module " + moduleModel.name()
                );
            }
            identities.add(identity);
        }
        for (ImportedServiceModel serviceModel : importedServiceModels)
        {
            String identity = serviceModel.identity().toString();
            if (identities.contains(identity))
            {
                throw new DuplicateServiceIdentityException(
                        "Duplicated service reference: " + identity + " in module " + moduleModel.name()
                );
            }
            identities.add(identity);
        }

        importedServiceModels
            .stream()
            .filter(
                importedServiceModel ->
                    objectModels.stream().noneMatch( model -> model.types().findFirst().get()
                                                                   .equals( importedServiceModel.serviceImporter() ) ) )
            .forEach(
                importedServiceModel ->
                    objectModels.add( new ObjectModel( moduleModel, importedServiceModel.serviceImporter(),
                                                       Visibility.module, new MetaInfo() ) ) );

        return moduleModel;
    }

    private void addDefaultAssemblers()
            throws AssemblyException
    {
        try
        {
            defaultAssemblers.entrySet().stream()
                    .filter(entry -> serviceAssemblies.stream().noneMatch(serviceAssembly -> serviceAssembly.hasType(entry.getKey())))
                    .forEach(entry ->
                    {
                        try
                        {
                            entry.getValue().assemble(this);
                        }
                        catch (AssemblyException e)
                        {
                            throw new UndeclaredThrowableException(e);
                        }
                    });
        }
        catch (UndeclaredThrowableException e)
        {
            throw (AssemblyException) e.getUndeclaredThrowable();
        }
    }

    private Identity generateId(Stream<Class<?>> serviceTypes)
    {
        // Find service reference that is not yet used
        Class<?> serviceType = serviceTypes.findFirst()
                .orElse(null); // Use the first, which *SHOULD* be the main serviceType
        int idx = 0;
        Identity id = new StringIdentity(serviceType.getSimpleName());
        boolean invalid;
        do
        {
            invalid = false;
            for (ServiceAssemblyImpl serviceAssembly : serviceAssemblies)
            {
                if (serviceAssembly.identity() != null && serviceAssembly.identity().equals(id))
                {
                    idx++;
                    id = new StringIdentity(serviceType.getSimpleName() + "_" + idx);
                    invalid = true;
                    break;
                }
            }
        }
        while (invalid);
        return id;
    }
}
