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

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.composite.CompositesModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.object.ObjectsModel;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.value.ValuesModel;
import org.qi4j.spi.structure.ModuleDescriptor;

/**
 * JAVADOC
 */
public class ModuleModel
    implements Binder, ModuleDescriptor, Serializable
{
    private LayerModel layerModel;
    private final CompositesModel compositesModel;
    private final EntitiesModel entitiesModel;
    private final ObjectsModel objectsModel;
    private final ValuesModel valuesModel;
    private final ServicesModel servicesModel;
    private ImportedServicesModel importedServicesModel;
    private transient ClassLoader classLoader;

    private final String name;
    private MetaInfo metaInfo;

    public ModuleModel( String name,
                        MetaInfo metaInfo, CompositesModel compositesModel,
                        EntitiesModel entitiesModel,
                        ObjectsModel objectsModel,
                        ValuesModel valuesModel,
                        ServicesModel servicesModel,
                        ImportedServicesModel importedServicesModel
    )
    {
        this.name = name;
        this.metaInfo = metaInfo;
        this.compositesModel = compositesModel;
        this.entitiesModel = entitiesModel;
        this.objectsModel = objectsModel;
        this.valuesModel = valuesModel;
        this.servicesModel = servicesModel;
        this.importedServicesModel = importedServicesModel;

        this.classLoader = new ModuleClassLoader( Thread.currentThread().getContextClassLoader() );
    }

    public String name()
    {
        return name;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
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

    public ImportedServicesModel importedServicesModel()
    {
        return importedServicesModel;
    }

    public ClassLoader classLoader()
    {
        return classLoader;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositesModel.visitModel( modelVisitor );
        entitiesModel.visitModel( modelVisitor );
        servicesModel.visitModel( modelVisitor );
        importedServicesModel.visitModel( modelVisitor );
        objectsModel.visitModel( modelVisitor );
        valuesModel.visitModel( modelVisitor );
    }

    public void visitModules( ModuleVisitor visitor )
    {
        // Visit this module
        if( !visitor.visitModule( null, this, Visibility.module ) )
        {
            return;
        }

        // Visit layer
        layerModel.visitModules( visitor, Visibility.layer );
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        layerModel = resolution.layer();

        resolution = new Resolution( resolution.application(), resolution.layer(), this, null, null, null );

        compositesModel.bind( resolution );
        entitiesModel.bind( resolution );
        servicesModel.bind( resolution );
        objectsModel.bind( resolution );
        valuesModel.bind( resolution );
    }

    // Context

    public ModuleInstance newInstance( LayerInstance layerInstance )
    {
        return new ModuleInstance( this, layerInstance, compositesModel, entitiesModel, objectsModel, valuesModel, servicesModel, importedServicesModel );
    }

    @Override
    public String toString()
    {
        return name;
    }

    private void readObject( java.io.ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        classLoader = new ModuleClassLoader( Thread.currentThread().getContextClassLoader() );
    }

    private class ModuleClassLoader
        extends ClassLoader
    {
        Map<String, Class> classes = new ConcurrentHashMap<String, Class>();

        private ModuleClassLoader( ClassLoader classLoader )
        {
            super( classLoader );
        }

        @Override
        protected Class<?> findClass( String name )
            throws ClassNotFoundException
        {
            Class clazz = classes.get( name );
            if( clazz == null )
            {
                ClassFinder finder = new ClassFinder();
                finder.type = name;
                visitModules( finder );

                if( finder.clazz == null )
                {
                    throw new ClassNotFoundException( name );
                }
                clazz = finder.clazz;
                classes.put( name, clazz );
            }

            return clazz;
        }
    }

    static class ClassFinder
        implements ModuleVisitor
    {
        public String type;
        public Class clazz;

        public boolean visitModule( ModuleInstance moduleInstance, ModuleModel moduleModel, Visibility visibility )
        {
            clazz = moduleModel.objects().getClassForName( type );
            if( clazz == null )
            {
                clazz = moduleModel.composites().getClassForName( type );
            }
            if( clazz == null )
            {
                clazz = moduleModel.entities().getClassForName( type );
            }
            if( clazz == null )
            {
                clazz = moduleModel.values().getClassForName( type );
            }

            return clazz == null;
        }
    }
}
