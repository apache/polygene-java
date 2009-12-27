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

package org.qi4j.library.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.jdbc.AggregatedException;
import org.qi4j.library.jdbc.ConnectionPool;
import org.qi4j.library.jdbc.ConnectionPoolConfiguration;

@Mixins( ConnectionPoolService.ConnectionPoolMixin.class)
public interface ConnectionPoolService extends ServiceComposite, ConnectionPool
{

    class ConnectionPoolMixin
        implements ConnectionPool, Activatable
    {

        @This
        private Configuration<ConnectionPoolConfiguration> config;

        private final LinkedList<Connection> freeConnections;
        private final LinkedList<Connection> handedOutConnections;

        public ConnectionPoolMixin()
        {
            freeConnections = new LinkedList<Connection>();
            handedOutConnections = new LinkedList<Connection>();
        }

        public Connection obtainConnection()
            throws SQLException
        {
            Connection conn;
            if( freeConnections.isEmpty() )
            {
                conn = createConnection();
            }
            else
            {
                conn = freeConnections.removeFirst();
            }
            handedOutConnections.addLast( conn );
            return conn;
        }

        public void releaseConnection( Connection connection )
            throws SQLException
        {
            handedOutConnections.remove( connection );
            if( connection.isClosed() )
                return; // can not keep a closed connection.
            freeConnections.addLast( connection );
        }

        private Connection createConnection()
            throws SQLException
        {
            config.refresh();
            String url = config.configuration().driverUrl().get();
            Driver driver = DriverManager.getDriver( url );
            String user = config.configuration().driverUsername().get();
            String password = config.configuration().driverPassword().get();
            Map<String,String> properties = config.configuration().driverProperties().get();
            properties.put( "user", user );
            properties.put( "password", password );
            Properties info = new Properties();
            info.putAll( properties );
            return driver.connect( url, info );
        }

        public void activate()
            throws Exception
        {
        }

        public void passivate()
            throws Exception
        {
            List<Exception> exceptions = new ArrayList<Exception>();
            for( Connection conn : handedOutConnections )
            {
                try
                {
                    conn.close();
                }
                catch( SQLException e )
                {
                    exceptions.add( e );
                }
            }
            for( Connection conn : freeConnections )
            {
                try
                {
                    conn.close();
                }
                catch( SQLException e )
                {
                    exceptions.add( e );
                }
            }
            if( ! exceptions.isEmpty() )
            {
                throw new AggregatedException( exceptions );
            }
        }
    }
}
