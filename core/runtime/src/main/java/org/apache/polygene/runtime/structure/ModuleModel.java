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
package org.apache.polygene.runtime.structure;

import java.util.stream.Stream;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.service.ImportedServiceDescriptor;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.structure.TypeLookup;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.runtime.activation.ActivatorsInstance;
import org.apache.polygene.runtime.activation.ActivatorsModel;
import org.apache.polygene.runtime.composite.TransientsModel;
import org.apache.polygene.runtime.entity.EntitiesModel;
import org.apache.polygene.runtime.object.ObjectsModel;
import org.apache.polygene.runtime.service.ImportedServicesModel;
import org.apache.polygene.runtime.service.ServicesModel;
import org.apache.polygene.runtime.value.ValuesModel;

import static java.util.stream.Stream.concat;
import static org.apache.polygene.api.common.Visibility.application;
import static org.apache.polygene.api.common.Visibility.layer;
import static org.apache.polygene.api.common.Visibility.module;

/**
 * JAVADOC
 */
public class ModuleModel
    implements ModuleDescriptor, VisitableHierarchy<Object, Object>
{
    private final LayerDescriptor layerModel;
    private final ActivatorsModel<Module> activatorsModel;
    private final TransientsModel transientsModel;
    private final EntitiesModel entitiesModel;
    private final ObjectsModel objectsModel;
    private final ValuesModel valuesModel;
    private final ServicesModel servicesModel;
    private final ImportedServicesModel importedServicesModel;
    private final TypeLookupImpl typeLookup;
    private final ClassLoader classLoader;

    private final String name;
    private final MetaInfo metaInfo;
    private ModuleInstance moduleInstance;

    public ModuleModel( String name,
                        MetaInfo metaInfo,
                        LayerDescriptor layerModel,
                        ActivatorsModel<Module> activatorsModel,
                        TransientsModel transientsModel,
                        EntitiesModel entitiesModel,
                        ObjectsModel objectsModel,
                        ValuesModel valuesModel,
                        ServicesModel servicesModel,
                        ImportedServicesModel importedServicesModel
    )
    {
        this.name = name;
        this.metaInfo = metaInfo;
        this.layerModel = layerModel;
        this.activatorsModel = activatorsModel;
        this.transientsModel = transientsModel;
        this.entitiesModel = entitiesModel;
        this.objectsModel = objectsModel;
        this.valuesModel = valuesModel;
        this.servicesModel = servicesModel;
        this.importedServicesModel = importedServicesModel;
        typeLookup = new TypeLookupImpl( this );
        classLoader = new ModuleClassLoader( this, Thread.currentThread().getContextClassLoader() );
    }

    @Override
    public String name()
    {
        return name;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    @Override
    public LayerDescriptor layer()
    {
        return layerModel;
    }

    @Override
    public ClassLoader classLoader()
    {
        return classLoader;
    }

    public ActivatorsInstance<Module> newActivatorsInstance()
        throws ActivationException
    {
        return new ActivatorsInstance<>( activatorsModel.newInstances() );
    }

    @Override
    public EntityDescriptor entityDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            EntityDescriptor entityModel = typeLookup.lookupEntityModel( type );
            if( entityModel == null )
            {
                return null;
            }
            return entityModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ObjectDescriptor objectDescriptor( String typeName )
    {
        try
        {
            Class<?> type = classLoader().loadClass( typeName );
            ObjectDescriptor objectModel = typeLookup.lookupObjectModel( type );
            if( objectModel == null )
            {
                return null;
            }
            return objectModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public TransientDescriptor transientDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            TransientDescriptor transientModel = typeLookup.lookupTransientModel( type );
            if( transientModel == null )
            {
                return null;
            }
            return transientModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ValueDescriptor valueDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ValueDescriptor valueModel = typeLookup.lookupValueModel( type );
            if( valueModel == null )
            {
                return null;
            }
            return valueModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public Module instance()
    {
        return moduleInstance;
    }

    @Override
    public TypeLookup typeLookup()
    {
        return typeLookup;
    }

    public ModuleInstance newInstance( LayerDescriptor layerInstance )
    {
        moduleInstance = new ModuleInstance( this, layerInstance, typeLookup, servicesModel, importedServicesModel );
        return moduleInstance;
    }

    @Override
    public Stream<? extends TransientDescriptor> transientComposites()
    {
        return transientsModel.stream();
    }

    @Override
    public Stream<? extends ValueDescriptor> valueComposites()
    {
        return valuesModel.stream();
    }

    @Override
    public Stream<? extends ServiceDescriptor> serviceComposites()
    {
        return servicesModel.models();
    }

    @Override
    public Stream<? extends EntityDescriptor> entityComposites()
    {
        return entitiesModel.stream();
    }

    @Override
    public Stream<? extends ImportedServiceDescriptor> importedServices()
    {
        return importedServicesModel.models();
    }

    @Override
    public Stream<? extends ObjectDescriptor> objects()
    {
        return objectsModel.models();
    }

    @Override
    public Stream<? extends ValueDescriptor> findVisibleValueTypes()
    {
        return concat( visibleValues( module ),
                       concat(
                           layer().visibleValues( layer ),
                           concat(
                               layer().visibleValues( application ),
                               layer().usedLayers().layers().flatMap( layer1 -> layer1.visibleValues( application ) )
                           )
                       )
        );
    }

    @Override
    public Stream<? extends EntityDescriptor> findVisibleEntityTypes()
    {
        return concat( visibleEntities( module ),
                       concat(
                           layer().visibleEntities( layer ),
                           concat(
                               layer().visibleEntities( application ),
                               layer().usedLayers().layers().flatMap( layer1 -> layer1.visibleEntities( application ) )
                           )
                       )
        );
    }

    @Override
    public Stream<? extends TransientDescriptor> findVisibleTransientTypes()
    {
        return concat( visibleTransients( module ),
                       concat(
                           layer().visibleTransients( layer ),
                           concat(
                               layer().visibleTransients( application ),
                               layer().usedLayers()
                                   .layers()
                                   .flatMap( layer1 -> layer1.visibleTransients( application ) )
                           )
                       )
        );
    }

    public Stream<? extends ModelDescriptor> findVisibleServiceTypes()
    {
        return concat( visibleServices( module ),
                       concat(
                           layer().visibleServices( layer ),
                           concat(
                               layer().visibleServices( application ),
                               layer().usedLayers().layers().flatMap( layer1 -> layer1.visibleServices( application ) )
                           )
                       )
        );
    }

    @Override
    public Stream<? extends ObjectDescriptor> findVisibleObjectTypes()
    {
        return concat( visibleObjects( module ),
                       concat(
                           layer().visibleObjects( layer ),
                           concat(
                               layer().visibleObjects( application ),
                               layer().usedLayers().layers().flatMap( layer -> layer.visibleObjects( application ) )
                           )
                       )
        );
    }

    public Stream<? extends ObjectDescriptor> visibleObjects( Visibility visibility )
    {
        return objectsModel.models()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends TransientDescriptor> visibleTransients( Visibility visibility )
    {
        return transientsModel.models()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends EntityDescriptor> visibleEntities( Visibility visibility )
    {
        return entitiesModel.models()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends ValueDescriptor> visibleValues( Visibility visibility )
    {
        return valuesModel.models()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends ModelDescriptor> visibleServices( Visibility visibility )
    {
        return concat(
            servicesModel.models()
                .filter( new VisibilityPredicate( visibility ) ),
            importedServicesModel.models()
                .filter( new VisibilityPredicate( visibility ) )
        );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            if( activatorsModel.accept( modelVisitor ) )
            {
                if( transientsModel.accept( modelVisitor ) )
                {
                    if( entitiesModel.accept( modelVisitor ) )
                    {
                        if( servicesModel.accept( modelVisitor ) )
                        {
                            if( importedServicesModel.accept( modelVisitor ) )
                            {
                                if( objectsModel.accept( modelVisitor ) )
                                {
                                    valuesModel.accept( modelVisitor );
                                }
                            }
                        }
                    }
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }

    @Override
    public String toString()
    {
        return name;
    }
}
