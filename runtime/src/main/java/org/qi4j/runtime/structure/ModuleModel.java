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

import java.io.Serializable;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.spi.structure.ModuleDescriptor;
import org.qi4j.api.common.MetaInfo;

/**
 * JAVADOC
 */
public class ModuleModel
    implements Binder, ModuleDescriptor, Serializable
{
    private final CompositesModel compositesModel;
    private final EntitiesModel entitiesModel;
    private final ObjectsModel objectsModel;
    private final ValuesModel valuesModel;
    private final ServicesModel servicesModel;

    private final String name;
    private MetaInfo metaInfo;

    public ModuleModel( String name,
                        MetaInfo metaInfo, CompositesModel compositesModel,
                        EntitiesModel entitiesModel,
                        ObjectsModel objectsModel,
                        ValuesModel valuesModel,
                        ServicesModel servicesModel )
    {
        this.name = name;
        this.metaInfo = metaInfo;
        this.compositesModel = compositesModel;
        this.entitiesModel = entitiesModel;
        this.objectsModel = objectsModel;
        this.valuesModel = valuesModel;
        this.servicesModel = servicesModel;
    }

    public String name()
    {
        return name;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    public CompositesModel composites()
    {
        return compositesModel;
    }

    public EntitiesModel entities()
    {
        return entitiesModel;
    }

    public ObjectsModel objects()
    {
        return objectsModel;
    }

    public ValuesModel values()
    {
        return valuesModel;
    }

    public ServicesModel services()
    {
        return servicesModel;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositesModel.visitModel( modelVisitor );
        entitiesModel.visitModel( modelVisitor );
        servicesModel.visitModel( modelVisitor );
        objectsModel.visitModel( modelVisitor );
        valuesModel.visitModel( modelVisitor );
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), this, null, null, null );

        compositesModel.bind( resolution );
        entitiesModel.bind( resolution );
        servicesModel.bind(resolution);
        objectsModel.bind( resolution );
        valuesModel.bind( resolution );
    }

    // Context
    public ModuleInstance newInstance( LayerInstance layerInstance )
    {
        return new ModuleInstance( this, layerInstance, compositesModel, entitiesModel, objectsModel, valuesModel, servicesModel );
    }

    @Override public String toString()
    {
        return name;
    }
}
