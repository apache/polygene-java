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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.index.rdf.RdfExport;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Show RDF index
 */
public class IndexResource
    extends ServerResource
{
    @Service private RdfExport export;

    public IndexResource()
    {
        getVariants().put(Method.ALL, Arrays.asList(
                MediaType.TEXT_HTML,
                MediaType.APPLICATION_RDF_XML));
        setNegotiated(true);
    }    

    @Override
    public Representation get( Variant variant ) throws ResourceException
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
