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

import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.serializer.SerializerContext;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.composite.TransientDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;

/**
 * JAVADOC
 */
class ApplicationVisitor extends DescriptorVisitor
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

    @Override public void visit( ApplicationDescriptor applicationDescriptor )
    {
        appUri = context.createApplicationUri( applicationDescriptor.name() );
        context.setNameAndType( appUri, applicationDescriptor.name(), Qi4jRdf.TYPE_APPLICATION );
    }

    @Override public void visit( LayerDescriptor layerDescriptor )
    {
        layerUri = context.createLayerUri( appUri, layerDescriptor.name() );
        context.setNameAndType( layerUri, layerDescriptor.name(), Qi4jRdf.TYPE_LAYER );
        context.addRelationship( appUri, Qi4jRdf.RELATIONSHIP_LAYER, layerUri );
    }

    @Override public void visit( ModuleDescriptor moduleDescriptor )
    {
        moduleUri = context.createModuleUri( layerUri, moduleDescriptor.name() );
        context.setNameAndType( layerUri, moduleDescriptor.name(), Qi4jRdf.TYPE_MODULE );

        context.addRelationship( layerUri, Qi4jRdf.RELATIONSHIP_MODULE, moduleUri );
    }

    @Override public void visit( TransientDescriptor compositeModel )
    {
        compositeUri = context.createCompositeUri( moduleUri, compositeModel.type() );
        context.addType( compositeUri, Qi4jRdf.TYPE_COMPOSITE );
        context.addRelationship( moduleUri, Qi4jRdf.RELATIONSHIP_COMPOSITE, compositeUri );
    }

    @Override public void visit( EntityDescriptor entityDescriptor )
    {
        compositeUri = context.createCompositeUri( moduleUri, entityDescriptor.type() );
        context.addType( compositeUri, Qi4jRdf.TYPE_ENTITY );
        context.addRelationship( moduleUri, Qi4jRdf.RELATIONSHIP_ENTITY, compositeUri );
    }

    @Override public void visit( ObjectDescriptor objectDescriptor )
    {
        compositeUri = context.createCompositeUri( moduleUri, objectDescriptor.type() );
        context.addType( compositeUri, Qi4jRdf.TYPE_OBJECT );
        context.addRelationship( moduleUri, Qi4jRdf.RELATIONSHIP_OBJECT, compositeUri );
    }

    @Override public void visit( CompositeMethodDescriptor compositeMethodDescriptor )
    {
        String compositeMethodUri = context.createCompositeMethodUri( compositeUri, compositeMethodDescriptor.method() );
        context.addType( compositeMethodUri, Qi4jRdf.TYPE_METHOD );
        context.addRelationship( compositeUri, Qi4jRdf.RELATIONSHIP_METHOD, compositeMethodUri );
        context.addStatement( compositeMethodUri, Qi4jRdf.RELATIONSHIP_MIXIN, Classes.toURI( compositeMethodDescriptor.mixin().mixinClass() ) );
    }
}