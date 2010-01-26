/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Collection;
import org.restlet.ext.atom.Service;
import org.restlet.ext.atom.Workspace;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class Qi4jServiceResource
    extends Resource
{
    public Qi4jServiceResource( @Uses Context context, @Uses Request request, @Uses Response response )
    {
        super( context, request, response );

        getVariants().add( new Variant( MediaType.TEXT_HTML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_ATOMPUB_SERVICE ) );
    }

    @Override
    public Representation represent( Variant variant )
        throws ResourceException
    {
        Service service = new Service( getContext().getServerDispatcher() );
        Workspace workspace = new Workspace( service, "Qi4j" );
        service.getWorkspaces().add( workspace );

        Reference root = getRequest().getResourceRef().getParentRef();

        Collection entities = new Collection( workspace, "Entities", root.clone().addSegment( "entity" ).toString() );
        workspace.getCollections().add( entities );

        Collection entityTypes = new Collection( workspace, "Entity types", root.clone()
            .addSegment( "entitytypes" ).toString() );
        workspace.getCollections().add( entityTypes );

        service.setMediaType( MediaType.APPLICATION_ATOMPUB_SERVICE );
        return service;
    }
}
