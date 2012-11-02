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

package org.qi4j.library.rest.admin;

import java.io.*;
import java.util.Arrays;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.index.rdf.indexing.RdfExporter;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Show RDF index
 */
public class IndexResource
    extends ServerResource
{
    @Service
    private RdfExporter exporter;

    public IndexResource()
    {
        getVariants().addAll( Arrays.asList(
            new Variant( MediaType.TEXT_PLAIN ),
            new Variant( MediaType.APPLICATION_RDF_TRIG ),
            new Variant( MediaType.APPLICATION_RDF_XML ) ) );

        setNegotiated( true );
    }

    @Override
    public Representation get( Variant variant )
        throws ResourceException
    {
        if( variant.getMediaType().equals( MediaType.APPLICATION_RDF_XML ) )
        {
            return new RdfXmlOutputRepresentation();
        }
        else if( variant.getMediaType().equals( MediaType.APPLICATION_RDF_TRIG ) )
        {
            return new RdfTrigOutputRepresentation( MediaType.APPLICATION_RDF_TRIG );
        }
        else if( variant.getMediaType().equals( MediaType.TEXT_PLAIN ) )
        {
            return new RdfTrigOutputRepresentation( MediaType.TEXT_PLAIN );
        }

        return null;
    }

    private class RdfTrigOutputRepresentation
        extends OutputRepresentation
    {
        public RdfTrigOutputRepresentation( MediaType mediaType )
        {
            super( mediaType );
        }

        @Override
        public void write( OutputStream outputStream )
            throws IOException
        {
            PrintStream ps = null;
            try
            {
                ps = new PrintStream( outputStream );
                exporter.exportReadableToStream( ps );
            }
            finally
            {
                if( ps != null )
                {
                    ps.close();
                }
            }
        }
    }

    private class RdfXmlOutputRepresentation
        extends OutputRepresentation
    {
        public RdfXmlOutputRepresentation()
        {
            super( MediaType.APPLICATION_RDF_XML );
        }

        @Override
        public void write( OutputStream outputStream )
            throws IOException
        {
            PrintWriter pw = null;
            try
            {
                OutputStreamWriter osw = new OutputStreamWriter( outputStream, "UTF-8" );
                pw = new PrintWriter( osw );
                exporter.exportFormalToWriter( pw );
                pw.flush();
            }
            finally
            {
                if( pw != null )
                {
                    pw.close();
                }
            }
        }
    }
}
