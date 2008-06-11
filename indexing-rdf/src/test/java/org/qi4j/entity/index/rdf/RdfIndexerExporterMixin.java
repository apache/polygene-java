/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.entity.index.rdf;

import java.io.OutputStream;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.qi4j.injection.scope.This;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public abstract class RdfIndexerExporterMixin
    implements RdfIndexerExporterComposite
{
    @This RdfQueryContext state;

    public void toRDF( final OutputStream outputStream )
    {
        RDFWriter rdfWriter = new RDFXMLPrettyWriter( outputStream );
        try
        {
            final RepositoryConnection connection = state.getRepository().getConnection();
            try
            {
                connection.prepareGraphQuery( QueryLanguage.SERQL, "CONSTRUCT * FROM {x} p {y}" ).evaluate( rdfWriter );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                if( connection != null )
                {
                    connection.commit();
                }
            }
        }
        catch( RepositoryException e )
        {
            e.printStackTrace();
        }

    }
}