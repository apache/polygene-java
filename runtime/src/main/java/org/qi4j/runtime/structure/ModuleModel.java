/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.composite.TransientsModel;
import org.qi4j.runtime.entity.EntitiesModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.object.ObjectsModel;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.runtime.value.ValuesModel;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;

import static org.qi4j.api.util.Iterables.*;
import static org.qi4j.api.util.Iterables.iterable;
import static org.qi4j.runtime.object.ObjectModel.modelTypeSpecification;
import static org.qi4j.runtime.structure.VisibilitySpecification.MODULE;

/**
 * JAVADOC
 */
public class ModuleModel
    implements Binder, ModuleDescriptor
{
    private final TransientsModel transientsModel;
    private final EntitiesModel entitiesModel;
    private final ObjectsModel objectsModel;
    private final ValuesModel valuesModel;
    private final ServicesModel servicesModel;
    private final ImportedServicesModel importedServicesModel;

    private final String name;
    private MetaInfo metaInfo;

    public ModuleModel( String name,
                        MetaInfo metaInfo, TransientsModel transientsModel,
                        EntitiesModel entitiesModel,
                        ObjectsModel objectsModel,
                        ValuesModel valuesModel,
                        ServicesModel servicesModel,
                        ImportedServicesModel importedServicesModel
    )
    {
        this.name = name;
        this.metaInfo = metaInfo;
        this.transientsModel = transientsModel;
        this.entitiesModel = entitiesModel;
        this.objectsModel = objectsModel;
        this.valuesModel = valuesModel;
        this.servicesModel = servicesModel;
        this.importedServicesModel = importedServicesModel;
    }

    public String name()
    {
        return name;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public <ThrowableType extends Throwable> void visitModel( ModelVisitor<ThrowableType> modelVisitor )
        throws ThrowableType
    {
        modelVisitor.visit( this );

        transientsModel.visitModel( modelVisitor );
        entitiesModel.visitModel( modelVisitor );
        servicesModel.visitModel( modelVisitor );
        importedServicesModel.visitModel( modelVisitor );
        objectsModel.visitModel( modelVisitor );
        valuesModel.visitModel( modelVisitor );
    }

    // Binding
    public void bind( Resolution resolution )
        throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), this, null, null, null );

        transientsModel.bind( resolution );
        entitiesModel.bind( resolution );
        servicesModel.bind( resolution );
        objectsModel.bind( resolution );
        valuesModel.bind( resolution );
    }

    // Context

    public ModuleInstance newInstance( LayerInstance layerInstance )
    {
        return new ModuleInstance( this, layerInstance, transientsModel, entitiesModel, objectsModel, valuesModel, servicesModel, importedServicesModel );
    }

    @Override
    public String toString()
    {
        return name;
    }
}
