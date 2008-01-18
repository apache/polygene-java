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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.CompositeMethodContext;
import org.qi4j.runtime.composite.MixinContext;
import org.qi4j.runtime.composite.ObjectContext;
import org.qi4j.runtime.property.AssociationContext;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.runtime.structure.ApplicationContext;
import org.qi4j.runtime.structure.LayerContext;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.composite.ObjectBinding;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.composite.ObjectResolution;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.ResolutionContext;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.service.ServiceProvider;
import org.qi4j.spi.structure.ApplicationBinding;
import org.qi4j.spi.structure.ApplicationModel;
import org.qi4j.spi.structure.ApplicationResolution;
import org.qi4j.spi.structure.AssociationDescriptor;
import org.qi4j.spi.structure.LayerBinding;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.LayerResolution;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.spi.structure.ModuleModel;
import org.qi4j.spi.structure.ModuleResolution;
import org.qi4j.spi.structure.PropertyDescriptor;

/**
 * TODO
 */
public final class ApplicationBuilder
{
    private Qi4jRuntime runtime;

    private ApplicationModel applicationModel;
    private ApplicationBinding applicationBinding;
    private ApplicationResolution applicationResolution;
    private Map<LayerModel, LayerAssembly> layerModelAssemblyMap = new HashMap<LayerModel, LayerAssembly>();
    private Map<LayerAssembly, LayerModel> layerAssemblyModelMap = new HashMap<LayerAssembly, LayerModel>();
    private Map<LayerModel, LayerResolution> layerResolutionMap = new HashMap<LayerModel, LayerResolution>();
    private Map<ModuleModel, ModuleAssembly> moduleModelAssemblyMap = new HashMap<ModuleModel, ModuleAssembly>();

    public ApplicationBuilder( Qi4jRuntime runtime )
    {
        this.runtime = runtime;
    }

    public ApplicationContext newApplicationContext( ApplicationAssembly applicationAssembly )
    {
        applicationModel = newApplicationModel( applicationAssembly );
        applicationResolution = newApplicationResolution( applicationModel );
        applicationBinding = newApplicationBinding( applicationResolution );
        ApplicationContext applicationContext = newApplicationContext( applicationBinding );
        return applicationContext;
    }

    // Application
    private ApplicationModel newApplicationModel( ApplicationAssembly applicationAssembly )
    {
        List<LayerModel> layerModels = new ArrayList<LayerModel>();

        // Instantiate Layers
        for( LayerAssembly layerAssembly : applicationAssembly.getLayerAssemblies() )
        {
            LayerModel layerModel = newLayerModel( layerAssembly );
            layerModels.add( layerModel );
            layerModelAssemblyMap.put( layerModel, layerAssembly );
            layerAssemblyModelMap.put( layerAssembly, layerModel );
        }

        // Create ApplicationModel
        ApplicationModel applicationModel = new ApplicationModel( layerModels, applicationAssembly.getName() );
        return applicationModel;
    }

    private ApplicationResolution newApplicationResolution( ApplicationModel applicationModel )
    {
        List<LayerResolution> layerResolutions = new ArrayList<LayerResolution>();

        // Resolve Layers
        for( LayerModel layerModel : applicationModel.getLayerModels() )
        {
            LayerResolution layerResolution = newLayerResolution( applicationModel, layerModel );
            layerResolutions.add( layerResolution );
            layerResolutionMap.put( layerModel, layerResolution );
        }

        // Create ApplicationResolution
        ApplicationResolution applicationResolution = new ApplicationResolution( applicationModel, layerResolutions );
        return applicationResolution;
    }

    private ApplicationBinding newApplicationBinding( ApplicationResolution applicationResolution )
    {
        List<LayerBinding> layerBindings = new ArrayList<LayerBinding>();

        // Bind Layers
        for( LayerResolution layerResolution : applicationResolution.getLayerResolutions() )
        {
            LayerBinding layerBinding = newLayerBinding( layerResolution );
            layerBindings.add( layerBinding );
        }

        // Create ApplicationBinding
        ApplicationBinding applicationBinding = new ApplicationBinding( applicationResolution, layerBindings );
        return applicationBinding;
    }

    private ApplicationContext newApplicationContext( ApplicationBinding applicationBinding )
    {
        List<LayerContext> layerContexts = new ArrayList<LayerContext>();
        for( LayerBinding layerBinding : applicationBinding.getLayerBindings() )
        {
            LayerContext layerContext = newLayerContext( layerBinding );
            layerContexts.add( layerContext );
        }

        ApplicationContext applicationContext = new ApplicationContext( applicationBinding, layerContexts );
        return applicationContext;
    }

    // Layer
    private LayerModel newLayerModel( LayerAssembly layerAssembly )
    {
        List<ModuleModel> modules = new ArrayList<ModuleModel>();
        Map<Class<? extends Composite>, ModuleModel> publicCompositeMap = new HashMap<Class<? extends Composite>, ModuleModel>();
        for( ModuleAssembly moduleAssembly : layerAssembly.getModuleAssemblies() )
        {
            ModuleModel moduleModel = newModuleModel( moduleAssembly );
            modules.add( moduleModel );
            moduleModelAssemblyMap.put( moduleModel, moduleAssembly );

            // Add public Composites in Module to Layer
            // Must be explicitly marked as public in the Layer to be added!
            Iterable<CompositeModel> publicModuleComposites = moduleModel.getPublicComposites();
            for( CompositeModel publicModuleComposite : publicModuleComposites )
            {
                if( layerAssembly.getPublicComposites().contains( publicModuleComposite.getCompositeClass() ) )
                {
                    publicCompositeMap.put( publicModuleComposite.getCompositeClass(), moduleModel );
                }
            }
        }

        LayerModel layerModel = new LayerModel( modules, publicCompositeMap, layerAssembly.getName() );
        return layerModel;
    }

    private LayerResolution newLayerResolution( ApplicationModel applicationModel, LayerModel layerModel )
    {
        List<ModuleResolution> moduleResolutions = new ArrayList<ModuleResolution>();
        for( ModuleModel moduleModel : layerModel.getModuleModels() )
        {
            ModuleResolution moduleResolution = newModuleResolution( applicationModel, layerModel, moduleModel );
            moduleResolutions.add( moduleResolution );
        }

        LayerAssembly layerAssembly = layerModelAssemblyMap.get( layerModel );
        List<LayerResolution> usedLayers = new ArrayList<LayerResolution>();
        for( LayerAssembly assembly : layerAssembly.getUses() )
        {
            LayerModel usedLayer = layerAssemblyModelMap.get( assembly );
            LayerResolution usedLayerResolution = layerResolutionMap.get( usedLayer );
            usedLayers.add( usedLayerResolution );
        }

        LayerResolution layerResolution = new LayerResolution( layerModel, applicationModel, moduleResolutions, usedLayers );
        return layerResolution;
    }

    private LayerBinding newLayerBinding( LayerResolution layerResolution )
    {
        List<ModuleBinding> moduleBindings = new ArrayList<ModuleBinding>();
        for( ModuleResolution moduleResolution : layerResolution.getModuleResolutions() )
        {
            ModuleBinding moduleBinding = newModuleBinding( layerResolution, moduleResolution );
            moduleBindings.add( moduleBinding );
        }

        // TODO Order modules according to inter-dependencies

        LayerBinding layerBinding = new LayerBinding( layerResolution, moduleBindings );
        return layerBinding;
    }

    private LayerContext newLayerContext( LayerBinding layerBinding )
    {
        List<ModuleContext> moduleContexts = new ArrayList<ModuleContext>();
        Iterable<ModuleBinding> moduleBindings = layerBinding.getModuleBindings();
        Map<ModuleModel, Map<Class<? extends Composite>, CompositeContext>> compositeContexts = new LinkedHashMap<ModuleModel, Map<Class<? extends Composite>, CompositeContext>>();
        for( ModuleBinding moduleBinding : moduleBindings )
        {
            Map<Class<? extends Composite>, CompositeContext> moduleCompositeContexts = new HashMap<Class<? extends Composite>, CompositeContext>();
            Map<String, PropertyContext> propertyContexts = new HashMap<String, PropertyContext>();
            Map<String, AssociationContext> associationContexts = new HashMap<String, AssociationContext>();
            for( Map.Entry<Class<? extends Composite>, CompositeBinding> entry : moduleBinding.getCompositeBindings().entrySet() )
            {
                CompositeBinding binding = entry.getValue();
                List<CompositeMethodContext> compositeMethodContexts = new ArrayList<CompositeMethodContext>();
                Iterable<CompositeMethodBinding> compositeMethodBindings = binding.getCompositeMethodBindings();
                for( CompositeMethodBinding compositeMethodBinding : compositeMethodBindings )
                {
                    PropertyContext propertyContext = null;
                    if( compositeMethodBinding.getPropertyBinding() != null )
                    {
                        propertyContext = new PropertyContext( compositeMethodBinding.getPropertyBinding() );
                        PropertyModel propertyModel = propertyContext.getPropertyBinding().getPropertyResolution().getPropertyModel();
                        propertyContexts.put( propertyModel.getQualifiedName(), propertyContext );
                    }

                    AssociationContext associationContext = null;
                    if( compositeMethodBinding.getAssociationBinding() != null )
                    {
                        associationContext = new AssociationContext( compositeMethodBinding.getAssociationBinding() );
                        AssociationModel associationModel = associationContext.getAssociationBinding().getAssociationResolution().getAssociationModel();
                        associationContexts.put( associationModel.getQualifiedName(), associationContext );
                    }

                    CompositeMethodContext compositeMethodContext = new CompositeMethodContext( compositeMethodBinding, applicationBinding, entry.getValue(), runtime, propertyContext, associationContext );
                    compositeMethodContexts.add( compositeMethodContext );
                }

                Set<MixinContext> mixinContexts = new HashSet<MixinContext>();
                for( MixinBinding mixinBinding : entry.getValue().getMixinBindings() )
                {
                    Set<PropertyContext> mixinProperties = new HashSet<PropertyContext>();
                    for( PropertyBinding propertyBinding : mixinBinding.getPropertyBindings() )
                    {
                        String propertyKey = propertyBinding.getPropertyResolution().getPropertyModel().getQualifiedName();
                        mixinProperties.add( propertyContexts.get( propertyKey ) );
                    }
                    Set<AssociationContext> mixinAssociations = new HashSet<AssociationContext>();
                    for( AssociationBinding associationBinding : mixinBinding.getAssociationBindings() )
                    {
                        String associationKey = associationBinding.getAssociationResolution().getAssociationModel().getQualifiedName();
                        mixinAssociations.add( associationContexts.get( associationKey ) );
                    }
                    MixinContext mixinContext = new MixinContext( mixinBinding, mixinProperties, mixinAssociations );
                    mixinContexts.add( mixinContext );
                }

                CompositeContext compositeContext = new CompositeContext( binding, compositeMethodContexts, moduleBinding, runtime.getInstanceFactory(), propertyContexts, mixinContexts, associationContexts );
                moduleCompositeContexts.put( entry.getKey(), compositeContext );
            }
            compositeContexts.put( moduleBinding.getModuleResolution().getModuleModel(), moduleCompositeContexts );
        }

        // TODO Is this really correct? Needs review!!
        Map<ModuleModel, ModuleContext> moduleModelContextMap = new HashMap<ModuleModel, ModuleContext>();
        for( ModuleBinding moduleBinding : moduleBindings )
        {
            ModuleContext moduleContext = newModuleContext( moduleBinding, compositeContexts, moduleModelContextMap );
            moduleContexts.add( moduleContext );
            moduleModelContextMap.put( moduleBinding.getModuleResolution().getModuleModel(), moduleContext );
        }
        LayerContext layerContext = new LayerContext( layerBinding, moduleContexts );
        return layerContext;
    }

    // Module
    private ModuleModel newModuleModel( ModuleAssembly moduleAssembly )
    {
        Set<Class<? extends Composite>> privateComposites = moduleAssembly.getPrivateComposites();

        List<PropertyDeclaration> propertyDeclarations = moduleAssembly.getPropertyDeclarations();
        Map<Method, PropertyDescriptor> propertyDescriptors = new HashMap<Method, PropertyDescriptor>();
        for( PropertyDeclaration propertyDeclaration : propertyDeclarations )
        {
            Method accessor = propertyDeclaration.getPropertyDescriptor().getAccessor();

            if( Composite.class.isAssignableFrom( accessor.getReturnType() ) )
            {
                if( !privateComposites.contains( accessor.getReturnType() ) )
                {
                    Class<? extends Composite> returnType = (Class<? extends Composite>) accessor.getReturnType();
                    moduleAssembly.addComposites( returnType );
                }
            }

            propertyDescriptors.put( accessor, propertyDeclaration.getPropertyDescriptor() );
        }

        List<AssociationDeclaration> associationDeclarations = moduleAssembly.getAssociationDeclarations();
        Map<Method, AssociationDescriptor> associationDescriptors = new HashMap<Method, AssociationDescriptor>();
        for( AssociationDeclaration associationDeclaration : associationDeclarations )
        {
            Method accessor = associationDeclaration.getAssociationDescriptor().getAccessor();

            if( Composite.class.isAssignableFrom( accessor.getReturnType() ) )
            {
                if( !privateComposites.contains( accessor.getReturnType() ) )
                {
                    Class<? extends Composite> returnType = (Class<? extends Composite>) accessor.getReturnType();
                    moduleAssembly.addComposites( returnType );
                }
            }

            associationDescriptors.put( accessor, associationDeclaration.getAssociationDescriptor() );
        }

        List<CompositeModel> privateCompositeModels = new ArrayList<CompositeModel>();
        for( Class<? extends Composite> compositeType : moduleAssembly.getPrivateComposites() )
        {
            CompositeModel compositeModel = runtime.getCompositeModelFactory().newCompositeModel( compositeType );
            privateCompositeModels.add( compositeModel );
        }

        List<CompositeModel> publicCompositeModels = new ArrayList<CompositeModel>();
        for( Class<? extends Composite> compositeType : moduleAssembly.getPublicComposites() )
        {
            CompositeModel compositeModel = runtime.getCompositeModelFactory().newCompositeModel( compositeType );
            publicCompositeModels.add( compositeModel );
        }

        List<ObjectModel> publicObjects = new ArrayList<ObjectModel>();
        for( Class<?> objectType : moduleAssembly.getPublicObjects() )
        {
            ObjectModel objectModel = runtime.getObjectModelFactory().newObjectModel( objectType );
            publicObjects.add( objectModel );
        }

        List<ObjectModel> privateObjects = new ArrayList<ObjectModel>();
        for( Class<?> objectType : moduleAssembly.getPrivateObjects() )
        {
            ObjectModel objectModel = runtime.getObjectModelFactory().newObjectModel( objectType );
            privateObjects.add( objectModel );
        }

        ModuleModel moduleModel = new ModuleModel( moduleAssembly.getServiceProviders(), moduleAssembly.getName(), publicCompositeModels, privateCompositeModels, publicObjects, privateObjects, propertyDescriptors, associationDescriptors );
        return moduleModel;
    }

    private ModuleResolution newModuleResolution( ApplicationModel applicationModel, LayerModel layerModel, ModuleModel moduleModel )
    {
        // Calculate set of instantiable Composites in this Module
        Map<Class<? extends Composite>, ModuleModel> instantiableComposites = new LinkedHashMap<Class<? extends Composite>, ModuleModel>();

        // Add public Composites from Modules in extended Layers
        Iterable<LayerAssembly> uses = layerModelAssemblyMap.get( layerModel ).getUses();
        for( LayerAssembly use : uses )
        {
            LayerModel usedLayerModel = layerAssemblyModelMap.get( use );
            addResolvableComposites( instantiableComposites, usedLayerModel.getPublicCompositeMap() );
        }

        // Add public Composites from Modules in this Layer
        addResolvableComposites( instantiableComposites, layerModel.getPublicCompositeMap() );

        // Add private Composites in Module itself
        Map<Class<? extends Composite>, ModuleModel> privateModuleComposites = getPrivateModuleComposites( moduleModel );
        addResolvableComposites( instantiableComposites, privateModuleComposites );

        // Resolve Composites in this Module
        List<CompositeResolution> compositeResolutions = new ArrayList<CompositeResolution>();
        resolveComposites( moduleModel.getPrivateComposites(), applicationModel, layerModel, moduleModel, compositeResolutions );
        resolveComposites( moduleModel.getPublicComposites(), applicationModel, layerModel, moduleModel, compositeResolutions );

        // Resolve objects in this Module
        List<ObjectResolution> objectResolutions = new ArrayList<ObjectResolution>();
        resolveObjects( moduleModel.getPublicObjects(), applicationModel, layerModel, moduleModel, objectResolutions );
        resolveObjects( moduleModel.getPrivateObjects(), applicationModel, layerModel, moduleModel, objectResolutions );

        // Figure out what service types are available from this module
        Map<Class, ServiceProvider> availableServices = new HashMap<Class, ServiceProvider>();
        availableServices.putAll( moduleModel.getServiceProviders() );

        // Add extended interfaces, unless they have been specifically registered already
        for( Map.Entry<Class, ServiceProvider> entry : moduleModel.getServiceProviders().entrySet() )
        {
            Class serviceType = entry.getKey();
            addSuperTypeProviders( serviceType, entry.getValue(), availableServices );
        }

        ModuleResolution moduleResolution = new ModuleResolution( moduleModel, applicationModel, layerModel, instantiableComposites, compositeResolutions, objectResolutions, availableServices );
        return moduleResolution;
    }

    private ModuleBinding newModuleBinding( LayerResolution layerResolution, ModuleResolution moduleResolution )
    {
        // Bind Composites in this Module
        Map<Class<? extends Composite>, CompositeBinding> compositeBindings = new LinkedHashMap<Class<? extends Composite>, CompositeBinding>();
        Iterable<CompositeResolution> compositeResolutions = moduleResolution.getCompositeResolutions();
        for( CompositeResolution compositeResolution : compositeResolutions )
        {
            BindingContext bindingContext = new BindingContext( null, null, compositeResolution, moduleResolution, layerResolution, applicationResolution );
            CompositeBinding compositeBinding = runtime.getCompositeBinder().bindCompositeResolution( bindingContext );

            compositeBindings.put( compositeResolution.getCompositeModel().getCompositeClass(), compositeBinding );
        }

        // Bind Objects in this Module
        Map<Class, ObjectBinding> objectBindings = new LinkedHashMap<Class, ObjectBinding>();
        for( ObjectResolution objectResolution : moduleResolution.getObjectResolutions() )
        {
            BindingContext bindingContext = new BindingContext( null, objectResolution, null, moduleResolution, layerResolution, applicationResolution );
            ObjectBinding objectBinding = runtime.getObjectBinder().bindObject( bindingContext );
            objectBindings.put( objectResolution.getObjectModel().getModelClass(), objectBinding );
        }

        ModuleBinding moduleBinding = new ModuleBinding( moduleResolution, compositeBindings, objectBindings );
        return moduleBinding;
    }

    private ModuleContext newModuleContext( ModuleBinding moduleBinding, Map<ModuleModel, Map<Class<? extends Composite>, CompositeContext>> compositeContexts, Map<ModuleModel, ModuleContext> moduleModelContextMap )
    {
        Map<Class<? extends Composite>, ModuleModel> instantiableComposites = moduleBinding.getModuleResolution().getInstantiableComposites();
        Map<Class<? extends Composite>, CompositeContext> instantiableCompositeContexts = new LinkedHashMap<Class<? extends Composite>, CompositeContext>();
        Map<Class<? extends Composite>, ModuleContext> instantiableModuleContexts = new HashMap<Class<? extends Composite>, ModuleContext>();
        for( Map.Entry<Class<? extends Composite>, ModuleModel> entry : instantiableComposites.entrySet() )
        {
            CompositeContext context = compositeContexts.get( entry.getValue() ).get( entry.getKey() );
            if( context != null )
            {
                addCompositeContext( instantiableCompositeContexts, entry.getKey(), context );
                addModuleContext( entry.getKey(), moduleModelContextMap.get( entry.getValue() ), instantiableModuleContexts );
            }
        }

        Map<Class, ObjectContext> instantiableObjectContexts = new LinkedHashMap<Class, ObjectContext>();
        for( Map.Entry<Class, ObjectBinding> entry : moduleBinding.getObjectBindings().entrySet() )
        {
            ObjectContext objectContext = new ObjectContext( entry.getValue(), moduleBinding, runtime.getInstanceFactory() );
            instantiableObjectContexts.put( entry.getKey(), objectContext );
        }

        ModuleContext moduleContext = new ModuleContext( moduleBinding, instantiableCompositeContexts, instantiableObjectContexts, instantiableModuleContexts );
        return moduleContext;
    }

    private void addSuperTypeProviders( Class serviceType, ServiceProvider provider, Map<Class, ServiceProvider> availableServices )
    {
        Class[] extendedInterfaces = serviceType.getInterfaces();
        for( Class extendedInterface : extendedInterfaces )
        {
            if( extendedInterface.getMethods().length > 0 )
            {
                ServiceProvider specificProvider = availableServices.get( extendedInterface );
                if( specificProvider == null )
                {
                    availableServices.put( extendedInterface, provider );
                }
                addSuperTypeProviders( extendedInterface, provider, availableServices );
            }
        }
    }

    private void resolveComposites( Iterable<CompositeModel> compositeModels, ApplicationModel applicationModel, LayerModel layerModel, ModuleModel moduleModel, List<CompositeResolution> compositeResolutions )
    {
        for( CompositeModel privateComposite : compositeModels )
        {
            ResolutionContext resolutionContext = new ResolutionContext( null, privateComposite, moduleModel, layerModel, applicationModel );
            CompositeResolution compositeResolution = runtime.getCompositeResolver().resolveCompositeModel( resolutionContext );
            compositeResolutions.add( compositeResolution );
        }
    }

    private void resolveObjects( Iterable<ObjectModel> objectModels, ApplicationModel applicationModel, LayerModel layerModel, ModuleModel moduleModel, List<ObjectResolution> objectResolutions )
    {
        for( ObjectModel objectModel : objectModels )
        {
            ResolutionContext resolutionContext = new ResolutionContext( objectModel, null, moduleModel, layerModel, applicationModel );
            ObjectResolution objectResolution = runtime.getObjectResolver().resolveObjectModel( resolutionContext );
            objectResolutions.add( objectResolution );
        }
    }

    private void addResolvableComposites( Map<Class<? extends Composite>, ModuleModel> resolvableComposites, Map<Class<? extends Composite>, ModuleModel> composites )
    {
        for( Map.Entry<Class<? extends Composite>, ModuleModel> entry : composites.entrySet() )
        {
            Class compositeClass = entry.getKey();
            ModuleModel moduleModel = entry.getValue();
            addResolvableComposite( resolvableComposites, compositeClass, moduleModel );
        }
    }

    private void addResolvableComposite( Map<Class<? extends Composite>, ModuleModel> resolvableComposites, Class compositeClass, ModuleModel moduleModel )
    {
        resolvableComposites.put( compositeClass, moduleModel );

        Class<? extends Composite> superComposite = runtime.getSuperComposite( compositeClass );
        if( superComposite != null )
        {
            addResolvableComposite( resolvableComposites, superComposite, moduleModel );
        }
    }

    private void addCompositeContext( Map<Class<? extends Composite>, CompositeContext> compositeContexts, Class compositeClass, CompositeContext compositeContext )
    {
        compositeContexts.put( compositeClass, compositeContext );

        Class<? extends Composite> superComposite = runtime.getSuperComposite( compositeClass );
        if( superComposite != null )
        {
            addCompositeContext( compositeContexts, superComposite, compositeContext );
        }
    }

    private <S extends Composite, T extends S> void addModuleContext( Class<T> compositeClass, ModuleContext moduleContext, Map<Class<? extends Composite>, ModuleContext> instantiableModuleContexts )
    {
        instantiableModuleContexts.put( compositeClass, moduleContext );

        Class<? extends S> superComposite = runtime.getSuperComposite( compositeClass );
        if( superComposite != null )
        {
            addModuleContext( superComposite, moduleContext, instantiableModuleContexts );
        }
    }

    private Map<Class<? extends Composite>, ModuleModel> getPrivateModuleComposites( ModuleModel moduleModel )
    {
        Map<Class<? extends Composite>, ModuleModel> privateModuleComposites = new LinkedHashMap<Class<? extends Composite>, ModuleModel>();
        Iterable<CompositeModel> privateComposites = moduleModel.getPrivateComposites();
        for( CompositeModel privateComposite : privateComposites )
        {
            privateModuleComposites.put( privateComposite.getCompositeClass(), moduleModel );
        }
        return privateModuleComposites;
    }

}
