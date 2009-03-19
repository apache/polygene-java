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

package org.qi4j.rest.query;

import java.io.IOException;
import java.io.OutputStream;
import org.qi4j.index.rdf.RdfExport;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.representation.Variant;
import org.restlet.representation.Representation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

/**
 * Show RDF index
 */
public class IndexResource
    extends Resource
{
    @Service private RdfExport export;

    public IndexResource( @Uses Context context, @Uses Request request, @Uses Response response )
        throws ClassNotFoundException
    {
        super( context, request, response );

        getVariants().add( new Variant( MediaType.APPLICATION_RDF_XML ) );
    }

    @Override @SuppressWarnings( "unused" )
    public Representation represent( Variant variant ) throws ResourceException
    {
        return new OutputRepresentation( MediaType.APPLICATION_RDF_XML )
        {
            public void write( OutputStream outputStream ) throws IOException
            {
                export.toRDF( outputStream );
            }
        };
    }
}
