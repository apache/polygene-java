/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.framework.rdf;

import org.qi4j.library.framework.rdf.parse.ApplicationParser;
import org.qi4j.library.framework.rdf.parse.StructureParser;
import org.qi4j.library.framework.rdf.serializer.N3Serializer;
import org.qi4j.library.framework.rdf.serializer.RdfXmlSerializer;
import org.qi4j.library.framework.rdf.serializer.TurtleSerializer;
import org.qi4j.runtime.structure.ApplicationContext;
import org.openrdf.model.Graph;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.RDFHandlerException;
import java.io.OutputStream;

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

    public  Serializer newSerializer( RdfFormat format )
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
}
