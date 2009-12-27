/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.index.sql.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.library.jdbc.ConnectionPool;
import org.qi4j.index.sql.callback.QualifiedIdentityResultCallback;
import org.qi4j.spi.query.EntityFinderException;

public class QueryExecutorMixin
    implements QueryExecutor
{
    @Service private ConnectionPool connectionPool;

    public long performQuery( String query, QualifiedIdentityResultCallback callback )
        throws EntityFinderException
    {
        try
        {
            Connection connection = connectionPool.obtainConnection();
            ResultSet resultSet = null;
            try
            {
                PreparedStatement st = connection.prepareStatement( query );
                resultSet = st.executeQuery();
                long row = 0;
                while( true )
                {
                    if( handleCallbacks( callback, resultSet, row ) )
                    {
                        break;
                    }
                    row++;
                }
                return row;
            }
            finally
            {
                if( resultSet != null )
                {
                    resultSet.close();
                }
                connectionPool.releaseConnection( connection );
            }
        }
        catch( SQLException e )
        {
            throw new EntityFinderException( e );
        }
        catch( Exception e )
        {
            throw new EntityFinderException( e );
        }
    }

    private boolean handleCallbacks( QualifiedIdentityResultCallback callback, ResultSet result, long row )
        throws Exception
    {
        if( ! result.next() )
            return true;

        if( callback != null )
        {
            final EntityReference entityReference = new EntityReference( result.getString( "ID" ) );
            if( !callback.processRow( row, entityReference ) )
            {
                return true;
            }
        }
        return false;
    }
}
