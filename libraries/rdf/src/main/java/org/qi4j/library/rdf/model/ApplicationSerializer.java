/*
 * Copyright 2007, 2008 Niclas Hedhman.
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
package org.qi4j.library.rdf.model;

import java.io.PrintWriter;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.qi4j.api.structure.Application;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.library.rdf.serializer.SerializerContext;

public class ApplicationSerializer
{
    public Graph serialize( Application app )
    {
        Graph graph = new GraphImpl();
        SerializerContext context = new SerializerContext( graph );
        ApplicationVisitor applicationVisitor = new ApplicationVisitor( context );
        ( (Application) app ).descriptor().accept( applicationVisitor );
        return graph;
    }

    public void outputMetadata( Graph rdf, PrintWriter writer )
        throws Exception
    {
        new RdfXmlSerializer().serialize( rdf, writer );
    }
}
