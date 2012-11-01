/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.index.rdf.query;

import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.spi.query.EntityFinderException;

@Mixins( TupleQueryExecutor.TupleQueryExecutorMixin.class )
public interface TupleQueryExecutor
{
    long performTupleQuery( QueryLanguage language, String query, @Optional Map<String, Object> bindings, @Optional QualifiedIdentityResultCallback callback )
        throws EntityFinderException;

    class TupleQueryExecutorMixin
        implements TupleQueryExecutor
    {
        @Service
        private Repository repository;

        @Override
        public long performTupleQuery( QueryLanguage language, String query, Map<String, Object> bindings, QualifiedIdentityResultCallback callback )
            throws EntityFinderException
        {
            try
            {
                RepositoryConnection connection = repository.getConnection();
                TupleQueryResult result = null;
                try
                {

                    TupleQuery tupleQuery = connection.prepareTupleQuery( language, query );

                    for (Map.Entry<String, Value> stringValueEntry : getBindings( bindings ).entrySet())
                    {
                        tupleQuery.setBinding(stringValueEntry.getKey(), stringValueEntry.getValue());
                    }

                    tupleQuery.setIncludeInferred( false );
                    result = tupleQuery.evaluate();
                    long row = 0;
                    while( result.hasNext() )
                    {
                        if( handleCallbacks( callback, result, row ) )
                        {
                            break;
                        }
                        row++;
                    }
                    return row;
                }
                finally
                {
                    if( result != null )
                    {
                        result.close();
                    }
                    if( connection != null )
                    {
                        connection.close();
                    }
                }
            }
            catch( RepositoryException e )
            {
                throw new EntityFinderException( e );
            }
            catch( MalformedQueryException e )
            {
                throw new EntityFinderException( e );
            }
            catch( QueryEvaluationException e )
            {
                throw new EntityFinderException( e );
            }
            catch( Exception e )
            {
                throw new EntityFinderException( e );
            }
        }

        private boolean handleCallbacks( QualifiedIdentityResultCallback callback, TupleQueryResult result, long row )
            throws Exception
        {
            BindingSet bindingSet = result.next();
            if( callback != null )
            {
                if( !processRow( row, bindingSet, callback ) )
                {
                    return true;
                }
            }
            return false;
        }

        private boolean processRow( long row, BindingSet bindingSet, QualifiedIdentityResultCallback callback )
        {
            final Value identifier = bindingSet.getValue( "identity" );

            //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
            if( identifier == null )
            {
                return true;
            }

            final String identity = identifier.stringValue();

            final EntityReference entityReference = new EntityReference( identity );
            return callback.processRow( row, entityReference );
        }

        private Map<String, Value> getBindings(Map<String, Object> variables)
        {
            Map<String, Value> bindings = new HashMap<String, Value>();
            for (Map.Entry<String, Object> stringObjectEntry : variables.entrySet())
            {
                if (!stringObjectEntry.getValue().getClass().equals(Object.class))
                    bindings.put(stringObjectEntry.getKey(), ValueFactoryImpl.getInstance().createLiteral(stringObjectEntry.getValue().toString()));
            }
            return bindings;
        }
    }
}
