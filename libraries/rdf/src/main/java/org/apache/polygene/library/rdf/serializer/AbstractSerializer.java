/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.library.rdf.serializer;

import java.io.Writer;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.apache.polygene.library.rdf.PolygeneRdf;
import org.apache.polygene.library.rdf.Rdfs;

abstract class AbstractSerializer
    implements Serializer
{
    private Class<? extends RDFWriterFactory> writerFactoryClass;

    protected AbstractSerializer( Class<? extends RDFWriterFactory> writerFactoryClass )
    {
        this.writerFactoryClass = writerFactoryClass;
    }

    @Override
    public void serialize( Iterable<Statement> graph, Writer out ) throws RDFHandlerException
    {
        String[] prefixes = { "polygene", "rdf", "rdfs" };
        String[] namespaces = { PolygeneRdf.POLYGENE_MODEL, Rdfs.RDF, Rdfs.RDFS };
        serialize( graph, out, prefixes, namespaces );
    }

    @Override
    public void serialize( Iterable<Statement> graph, Writer out, String[] namespacePrefixes, String[] namespaces )
        throws RDFHandlerException
    {
        RDFWriterFactory writerFactory;
        try
        {
            writerFactory = writerFactoryClass.newInstance();
        }
        catch( InstantiationException e )
        {
            throw new InternalError();
        }
        catch( IllegalAccessException e )
        {
            throw new InternalError();
        }
        RDFWriter writer = writerFactory.getWriter( out );
        writer.startRDF();
        for( int i = 0; i < namespacePrefixes.length; i++ )
        {
            String namespacePrefix = namespacePrefixes[ i ];
            String namespace = namespaces[ i ];
            writer.handleNamespace( namespacePrefix, namespace );
        }
        for( Statement st : graph )
        {
            writer.handleStatement( st );
        }
        writer.endRDF();
    }

}
