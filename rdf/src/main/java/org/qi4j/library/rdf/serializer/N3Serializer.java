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
package org.qi4j.library.rdf.serializer;

import java.io.Writer;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3WriterFactory;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.library.rdf.Serializer;

public final class N3Serializer
    implements Serializer
{
    public void serialize( Graph graph, Writer out )
        throws RDFHandlerException
    {
        RDFWriter writer = new N3WriterFactory().getWriter( out );
        writer.startRDF();
        writer.handleNamespace( "qi4j", Qi4jRdf.QI4JMODEL );
        writer.handleNamespace( "rdf", Rdfs.RDF );
        writer.handleNamespace( "rdfs", Rdfs.RDFS );
        for( Statement st : graph )
        {
            writer.handleStatement( st );
        }
        writer.endRDF();
    }
}
