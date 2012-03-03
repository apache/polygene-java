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

package org.qi4j.library.rdf.model;

import org.qi4j.api.composite.MethodDescriptor;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.serializer.SerializerContext;

import static org.qi4j.functional.Iterables.first;

/**
 * JAVADOC
 */
class ApplicationVisitor extends HierarchicalVisitorAdapter<Object, Object, RuntimeException>
{
    private SerializerContext context;

    private String appUri;

    private String layerUri;

    private String moduleUri;

    private String compositeUri;

    ApplicationVisitor( SerializerContext context )
    {
        this.context = context;
    }

    @Override
    public boolean visitEnter( Object visited )
        throws RuntimeException
    {
        if( visited instanceof ApplicationDescriptor )
        {
            ApplicationDescriptor applicationDescriptor = (ApplicationDescriptor) visited;
            appUri = context.createApplicationUri( applicationDescriptor.name() );
            context.setNameAndType( appUri, applicationDescriptor.name(), Qi4jRdf.TYPE_APPLICATION );
        }

        if( visited instanceof LayerDescriptor )
        {
            LayerDescriptor layerDescriptor = (LayerDescriptor) visited;
            layerUri = context.createLayerUri( appUri, layerDescriptor.name() );
            context.setNameAndType( layerUri, layerDescriptor.name(), Qi4jRdf.TYPE_LAYER );
            context.addRelationship( appUri, Qi4jRdf.RELATIONSHIP_LAYER, layerUri );
        }

        if( visited instanceof ModuleDescriptor )
        {
            ModuleDescriptor moduleDescriptor = (ModuleDescriptor) visited;
            moduleUri = context.createModuleUri( layerUri, moduleDescriptor.name() );
            context.setNameAndType( layerUri, moduleDescriptor.name(), Qi4jRdf.TYPE_MODULE );

            context.addRelationship( layerUri, Qi4jRdf.RELATIONSHIP_MODULE, moduleUri );
        }

        if( visited instanceof TransientDescriptor )
        {
            TransientDescriptor transientDescriptor = (TransientDescriptor) visited;
            compositeUri = context.createCompositeUri( moduleUri, first( transientDescriptor.types() ) );
            context.addType( compositeUri, Qi4jRdf.TYPE_COMPOSITE );
            context.addRelationship( moduleUri, Qi4jRdf.RELATIONSHIP_COMPOSITE, compositeUri );
        }

        if( visited instanceof EntityDescriptor )
        {
            EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
            compositeUri = context.createCompositeUri( moduleUri, first( entityDescriptor.types() ) );
            context.addType( compositeUri, Qi4jRdf.TYPE_ENTITY );
            context.addRelationship( moduleUri, Qi4jRdf.RELATIONSHIP_ENTITY, compositeUri );
        }

        if( visited instanceof ObjectDescriptor )
        {
            ObjectDescriptor objectDescriptor = (ObjectDescriptor) visited;
            compositeUri = context.createCompositeUri( moduleUri, first( objectDescriptor.types() ) );
            context.addType( compositeUri, Qi4jRdf.TYPE_OBJECT );
            context.addRelationship( moduleUri, Qi4jRdf.RELATIONSHIP_OBJECT, compositeUri );
        }

        if( visited instanceof MethodDescriptor )
        {
            MethodDescriptor compositeMethodDescriptor = (MethodDescriptor) visited;
            String compositeMethodUri = context.createCompositeMethodUri( compositeUri, compositeMethodDescriptor.method() );
            context.addType( compositeMethodUri, Qi4jRdf.TYPE_METHOD );
            context.addRelationship( compositeUri, Qi4jRdf.RELATIONSHIP_METHOD, compositeMethodUri );
        }

        return true;
    }
}