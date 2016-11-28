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

package org.apache.zest.runtime.bootstrap;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.zest.api.activation.Activator;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.identity.HasIdentity;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.IdentityGenerator;
import org.apache.zest.api.identity.StringIdentity;
import org.apache.zest.api.service.DuplicateServiceIdentityException;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.type.HasTypes;
import org.apache.zest.api.type.MatchTypeSpecification;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.AssemblySpecifications;
import org.apache.zest.bootstrap.AssemblyVisitor;
import org.apache.zest.bootstrap.ConfigurationDeclaration;
import org.apache.zest.bootstrap.EntityAssembly;
import org.apache.zest.bootstrap.EntityDeclaration;
import org.apache.zest.bootstrap.ImportedServiceAssembly;
import org.apache.zest.bootstrap.ImportedServiceDeclaration;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.MetaInfoDeclaration;
import org.apache.zest.bootstrap.MixinDeclaration;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.ObjectAssembly;
import org.apache.zest.bootstrap.ObjectDeclaration;
import org.apache.zest.bootstrap.ServiceAssembly;
import org.apache.zest.bootstrap.ServiceDeclaration;
import org.apache.zest.bootstrap.TransientAssembly;
import org.apache.zest.bootstrap.TransientDeclaration;
import org.apache.zest.bootstrap.ValueAssembly;
import org.apache.zest.bootstrap.ValueDeclaration;
import org.apache.zest.bootstrap.identity.DefaultIdentityGeneratorAssembler;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.zest.runtime.activation.ActivatorsModel;
import org.apache.zest.runtime.composite.TransientModel;
import org.apache.zest.runtime.composite.TransientsModel;
import org.apache.zest.runtime.entity.EntitiesModel;
import org.apache.zest.runtime.entity.EntityModel;
import org.apache.zest.runtime.object.ObjectModel;
import org.apache.zest.runtime.object.ObjectsModel;
import org.apache.zest.runtime.service.ImportedServiceModel;
import org.apache.zest.runtime.service.ImportedServicesModel;
import org.apache.zest.runtime.service.ServiceModel;
import org.apache.zest.runtime.service.ServicesModel;
import org.apache.zest.runtime.structure.LayerModel;
import org.apache.zest.runtime.structure.ModuleModel;
import org.apache.zest.runtime.value.ValueModel;
import org.apache.zest.runtime.value.ValuesModel;

import static java.util.stream.Collectors.toList;
import static org.apache.zest.functional.Iterables.iterable;

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
        defaultAssemblers.put(UnitOfWorkFactory.class, new DefaultUnitOfWorkAssembler());
        defaultAssemblers.put(IdentityGenerator.class, new DefaultIdentityGeneratorAssembler());
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
        this.activators.addAll(Arrays.asList(activators));
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
    public ConfigurationDeclaration configurations(Predicate<HasTypes> specification)
    {
        Predicate<HasTypes> isConfigurationComposite = new MatchTypeSpecification(HasIdentity.class);
        specification = specification.and(isConfigurationComposite);
        List<EntityAssemblyImpl> entityAssmblyList = new ArrayList<>();
        for (EntityAssemblyImpl entityAssembly : entityAssemblies.values())
        {
            if (specification.test(entityAssembly))
            {
                entityAssmblyList.add(entityAssembly);
            }
        }
        List<ValueAssemblyImpl> valueAssemblyList = new ArrayList<>();
        for (ValueAssemblyImpl transientAssembly : valueAssemblies.values())
        {
            if (specification.test(transientAssembly))
            {
                valueAssemblyList.add(transientAssembly);
            }
        }
        return new ConfigurationDeclarationImpl(entityAssmblyList, valueAssemblyList);
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
                throw new AssemblyException("Interfaces can not be Zest Objects.");
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
        visitor.visitModule(this);

        for (TransientAssemblyImpl compositeDeclaration : transientAssemblies.values())
        {
            visitor.visitComposite(new TransientDeclarationImpl(iterable(compositeDeclaration)));
        }

        for (EntityAssemblyImpl entityDeclaration : entityAssemblies.values())
        {
            visitor.visitEntity(new EntityDeclarationImpl(iterable(entityDeclaration)));
        }

        for (ObjectAssemblyImpl objectDeclaration : objectAssemblies.values())
        {
            visitor.visitObject(new ObjectDeclarationImpl(iterable(objectDeclaration)));
        }

        for (ServiceAssemblyImpl serviceDeclaration : serviceAssemblies)
        {
            visitor.visitService(new ServiceDeclarationImpl(iterable(serviceDeclaration)));
        }

        for (ImportedServiceAssemblyImpl importedServiceDeclaration : importedServiceAssemblies.values())
        {
            visitor.visitImportedService(new ImportedServiceDeclarationImpl(iterable(importedServiceDeclaration)));
        }

        for (ValueAssemblyImpl valueDeclaration : valueAssemblies.values())
        {
            visitor.visitValue(new ValueDeclarationImpl(iterable(valueDeclaration)));
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

        importedServiceModels.stream().filter(importedServiceModel ->
                !StreamSupport.stream(objectModels.spliterator(), false)
                        .anyMatch(model -> model.types().findFirst().get().equals(importedServiceModel.serviceImporter())))
                .forEach(importedServiceModel ->
        {
            Class<? extends ServiceImporter> serviceFactoryType = importedServiceModel.serviceImporter();
            ObjectModel objectModel = new ObjectModel(moduleModel, serviceFactoryType, Visibility.module, new MetaInfo());
            objectModels.add(objectModel);
        });

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
