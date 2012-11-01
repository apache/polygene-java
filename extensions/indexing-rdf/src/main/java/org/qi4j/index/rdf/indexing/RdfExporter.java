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

package org.qi4j.index.rdf.indexing;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.spi.query.IndexExporter;

/**
 * JAVADOC
 */
@Mixins( RdfExporter.RdfExporterMixin.class )
public interface RdfExporter
    extends IndexExporter
{
    /**
     * JAVADOC
     */
    class RdfExporterMixin
        implements IndexExporter
    {
        @Service
        private Repository repository;

        @Override
        public void exportReadableToStream( PrintStream out )
            throws IOException
        {
            RDFWriter rdfWriter = Rio.createWriter( RDFFormat.TRIG, out );
            try
            {
                final RepositoryConnection connection = repository.getConnection();
                try
                {
                    connection.export( rdfWriter );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    connection.close();
                }
            }
            catch( RepositoryException e )
            {
                throw new IOException( e );
            }
        }

        @Override
        public void exportFormalToWriter( PrintWriter out )
            throws IOException
        {
            RDFWriter rdfWriter = Rio.createWriter( RDFFormat.RDFXML, out );
            try
            {
                final RepositoryConnection connection = repository.getConnection();
                try
                {
                    connection.export( rdfWriter );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    connection.close();
                }
            }
            catch( RepositoryException e )
            {
                throw new IOException( e );
            }
        }
    }
}
