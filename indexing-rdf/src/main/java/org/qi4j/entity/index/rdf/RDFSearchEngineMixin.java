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

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.qi4j.composite.scope.ThisCompositeAs;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public class RDFSearchEngineMixin
    implements SearchEngine
{

    @ThisCompositeAs RDFIndexerState state;

    public Iterable<String> findbyNativeQuery( String query )
    {
        // TODO what about a null queryobsolete shall it fail?, return null or return empty
        if( query == null )
        {
            return null;
        }
        try
        {
            final RepositoryConnection connection = state.getRepository().getConnection();
            GraphQuery graphQuery = connection.prepareGraphQuery( QueryLanguage.SERQL, query );
            GraphQueryResult queryResult = graphQuery.evaluate();
            while( queryResult.hasNext() )
            {
                Statement statement = queryResult.next();
                System.out.println( "Result: " + statement.getObject() );
            }
            RDFWriter rdfWriter = new RDFXMLPrettyWriter( System.out );
            try
            {
                graphQuery.evaluate( rdfWriter );
            }
            catch( RDFHandlerException e )
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        catch( RepositoryException e )
        {
            e.printStackTrace();
        }
        catch( MalformedQueryException e )
        {
            // TODO shall it throw exception?
            e.printStackTrace();
        }
        catch( QueryEvaluationException e )
        {
            // TODO shall it throw exception?
            e.printStackTrace();
        }
        return null;
    }
}