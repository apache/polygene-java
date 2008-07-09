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

package org.qi4j.library.rdf.parse.model;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.parse.ParseContext;
import org.qi4j.structure.Layer;
import org.qi4j.structure.Application;
import org.qi4j.structure.Module;
import org.qi4j.composite.Composite;

/**
 * TODO
 */
public class ApplicationVisitor
//    extends ModelVisitor
{
    private ParseContext context;

    private URI appUri;
    private URI layerUri;
    private Layer layerModel;
    private Module moduleModel;
    private URI moduleUri;
    private URI compositeUri;
    private Resource compositeMethodUri;

    public ApplicationVisitor( ParseContext context )
    {
        this.context = context;
        appUri = context.getApplicationURI();
    }

    public void visit( Application applicationModel )
    {
        String name = applicationModel.toURI();
        context.addName( appUri, name );
        context.addType( appUri, Qi4jRdf.TYPE_APPLICATION );
    }

    public void visit( Layer layerModel )
    {
        this.layerModel = layerModel;
        layerUri = context.createLayerUri( layerModel );
        context.addName( layerUri, layerModel.name() );
        context.addType( layerUri, Qi4jRdf.TYPE_LAYER );

        context.addRelationship( appUri, Qi4jRdf.RELATIONSHIP_LAYER, layerUri );
    }

    public void visit( Module moduleModel )
    {
        this.moduleModel = moduleModel;
        moduleUri = context.createModuleUri( layerModel, moduleModel );
        context.addName( moduleUri, moduleModel.name() );
        context.addType( moduleUri, Qi4jRdf.TYPE_MODULE );

        context.addRelationship( layerUri, Qi4jRdf.RELATIONSHIP_MODULE, moduleUri );
    }

    public void visit( Composite compositeModel )
    {
        compositeUri = context.createCompositeUri( layerModel, moduleModel, compositeModel.type() );

        context.addType( compositeUri, Qi4jRdf.TYPE_COMPOSITE );

        context.addRelationship( moduleUri, Qi4jRdf.RELATIONSHIP_COMPOSITE, compositeUri );
    }

//    @Override public void visit( CompositeMethodModel compositeMethodModel )
//    {
//        compositeMethodUri = context.getValueFactory().createBNode( compositeMethodModel.method().getName() );
//
//        context.addName( compositeMethodUri, compositeMethodModel.method().getName() );
//
//        context.addRelationship( compositeUri, Qi4jRdf.RELATION_METHOD, compositeMethodUri );
//    }
}