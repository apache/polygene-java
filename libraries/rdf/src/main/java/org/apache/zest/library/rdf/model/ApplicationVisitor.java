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

package org.apache.zest.library.rdf.model;

import org.apache.zest.api.composite.MethodDescriptor;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.LayerDescriptor;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.functional.HierarchicalVisitorAdapter;
import org.apache.zest.library.rdf.ZestRdf;
import org.apache.zest.library.rdf.serializer.SerializerContext;

import static org.apache.zest.functional.Iterables.first;

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
            context.setNameAndType( appUri, applicationDescriptor.name(), ZestRdf.TYPE_APPLICATION );
        }

        if( visited instanceof LayerDescriptor )
        {
            LayerDescriptor layerDescriptor = (LayerDescriptor) visited;
            layerUri = context.createLayerUri( appUri, layerDescriptor.name() );
            context.setNameAndType( layerUri, layerDescriptor.name(), ZestRdf.TYPE_LAYER );
            context.addRelationship( appUri, ZestRdf.RELATIONSHIP_LAYER, layerUri );
        }

        if( visited instanceof ModuleDescriptor )
        {
            ModuleDescriptor moduleDescriptor = (ModuleDescriptor) visited;
            moduleUri = context.createModuleUri( layerUri, moduleDescriptor.name() );
            context.setNameAndType( layerUri, moduleDescriptor.name(), ZestRdf.TYPE_MODULE );

            context.addRelationship( layerUri, ZestRdf.RELATIONSHIP_MODULE, moduleUri );
        }

        if( visited instanceof TransientDescriptor )
        {
            TransientDescriptor transientDescriptor = (TransientDescriptor) visited;
            compositeUri = context.createCompositeUri( moduleUri, first( transientDescriptor.types() ) );
            context.addType( compositeUri, ZestRdf.TYPE_COMPOSITE );
            context.addRelationship( moduleUri, ZestRdf.RELATIONSHIP_COMPOSITE, compositeUri );
        }

        if( visited instanceof EntityDescriptor )
        {
            EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
            compositeUri = context.createCompositeUri( moduleUri, first( entityDescriptor.types() ) );
            context.addType( compositeUri, ZestRdf.TYPE_ENTITY );
            context.addRelationship( moduleUri, ZestRdf.RELATIONSHIP_ENTITY, compositeUri );
        }

        if( visited instanceof ObjectDescriptor )
        {
            ObjectDescriptor objectDescriptor = (ObjectDescriptor) visited;
            compositeUri = context.createCompositeUri( moduleUri, first( objectDescriptor.types() ) );
            context.addType( compositeUri, ZestRdf.TYPE_OBJECT );
            context.addRelationship( moduleUri, ZestRdf.RELATIONSHIP_OBJECT, compositeUri );
        }

        if( visited instanceof MethodDescriptor )
        {
            MethodDescriptor compositeMethodDescriptor = (MethodDescriptor) visited;
            String compositeMethodUri = context.createCompositeMethodUri( compositeUri, compositeMethodDescriptor.method() );
            context.addType( compositeMethodUri, ZestRdf.TYPE_METHOD );
            context.addRelationship( compositeUri, ZestRdf.RELATIONSHIP_METHOD, compositeMethodUri );
        }

        return true;
    }
}