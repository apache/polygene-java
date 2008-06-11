/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.library.rdf;

import java.io.IOException;
import java.io.Writer;
import org.openrdf.model.Graph;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.qi4j.library.rdf.parse.StructureParser;
import org.qi4j.library.rdf.serializer.N3Serializer;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.library.rdf.serializer.TurtleSerializer;

public class RdfFactory
{
    private static RdfFactory instance;

    static
    {
        instance = new RdfFactory();
    }

    private RdfFactory()
    {
    }

    public static RdfFactory getInstance()
    {
        return instance;
    }

    public StructureParser newStructureParser()
    {
        return new StructureParser();
    }

    public Serializer newSerializer( RdfFormat format )
        throws RDFHandlerException
    {
        Serializer serializer;
        if( format == RdfFormat.n3 )
        {
            serializer = new N3Serializer();
        }
        else if( format == RdfFormat.rdfxml )
        {
            serializer = new RdfXmlSerializer();
        }
        else if( format == RdfFormat.turtle )
        {
            serializer = new TurtleSerializer();
        }
        else
        {
            throw new UnsupportedRDFormatException( "Unsupported format: " + format );
        }
        return serializer;
    }

    public void serialize( ApplicationBinding binding, String applicationUri, RdfFormat format, Writer out )
        throws IOException
    {
        RdfFactory factory = RdfFactory.getInstance();

        // Parse application
        StructureParser parser = factory.newStructureParser();
        Graph graph = parser.parse( binding, applicationUri );

        // Serialize it
        try
        {
            Serializer serializer = factory.newSerializer( format );
            serializer.serialize( graph, out );
        }
        catch( RDFHandlerException e )
        {
            throw (IOException) new IOException().initCause( e );
        }

    }
}
