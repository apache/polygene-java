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
package org.qi4j.library.framework.rdf.serializer;

import java.io.OutputStream;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriterFactory;
import org.qi4j.library.framework.rdf.Qi4jRdf;
import org.qi4j.library.framework.rdf.Rdfs;
import org.qi4j.library.framework.rdf.Serializer;

public final class RdfXmlSerializer
    implements Serializer
{
    public void serialize( Graph graph, OutputStream out ) throws RDFHandlerException
    {
        RDFWriter writer = new RDFXMLPrettyWriterFactory().getWriter( out );
        writer.startRDF();
        writer.handleNamespace( "qi4j", Qi4jRdf.QI4J );
        writer.handleNamespace( "rdf", Rdfs.RDF );
        writer.handleNamespace( "rdfs", Rdfs.RDFS );
        for( Statement st : graph )
        {
            writer.handleStatement( st );
        }
        writer.endRDF();
    }
}
