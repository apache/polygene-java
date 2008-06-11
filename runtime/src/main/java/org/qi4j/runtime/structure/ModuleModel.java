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

import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;

/**
 * TODO
 */
public class ModuleModel
    implements Binder
{
    private CompositesModel compositesModel;
    private EntitiesModel entitiesModel;
    private ObjectsModel objectsModel;
    private ServicesModel servicesModel;

    private String name;
    private LayerModel layerComposite;

    public ModuleModel( String name,
                        LayerModel layerComposite,
                        CompositesModel compositesModel,
                        EntitiesModel entitiesModel,
                        ObjectsModel objectsModel,
                        ServicesModel servicesModel )
    {
        this.name = name;
        this.layerComposite = layerComposite;
        this.compositesModel = compositesModel;
        this.entitiesModel = entitiesModel;
        this.objectsModel = objectsModel;
        this.servicesModel = servicesModel;
    }

    public String name()
    {
        return name;
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

    public ServicesModel services()
    {
        return servicesModel;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositesModel.visitModel( modelVisitor );
        entitiesModel.visitModel( modelVisitor );
        objectsModel.visitModel( modelVisitor );
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), this, null, null, null );

        compositesModel.bind( resolution );
        entitiesModel.bind( resolution );
        objectsModel.bind( resolution );
    }

    // Context
    public ModuleInstance newInstance( LayerInstance layerInstance )
    {
        return new ModuleInstance( this, layerInstance, compositesModel, entitiesModel, objectsModel, servicesModel );
    }

    @Override public String toString()
    {
        return name;
    }
}
