/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis.dbInitializer;

import com.ibatis.common.jdbc.ScriptRunner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.property.Property;

/**
 * {@code DBIntializer} initialize the db.
 *
 */
public final class DBInitializer
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final DBInitializerConfiguration dBInitializerInfo;

    /**
     * Construct an instance of {@code DBInitializerInfo}.
     *
     * @param aDBInitializerInfo The db initializer info. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@code aDBInitializerInfo} argument is {@code null}.
     * @since 0.1.0
     */
    public DBInitializer( final DBInitializerConfiguration aDBInitializerInfo )
        throws IllegalArgumentException
    {
        dBInitializerInfo = aDBInitializerInfo;
        validateNotNull( "aDBInitializerInfo", aDBInitializerInfo );
    }

    /**
     * Initialize the database.
     * Reads scripts from schemaURL and data scripts from dataUrl
     *
     * @throws java.sql.SQLException Thrown if db initialization failed.
     * @throws java.io.IOException   Thrown if reading schema or data sql resources failed.
     * @since 0.1.0
     *        todo close reader, connections, handle exceptions
     */
    public final void initialize()
        throws SQLException, IOException
    {
        final Property<String> schemaUrlProperty = dBInitializerInfo.schemaUrl();
        final String schemaUrl = schemaUrlProperty.get();
        runScript( schemaUrl );
        runScript( dBInitializerInfo.dataUrl().get() );
    }

    private void runScript( final String urlString )
        throws SQLException, IOException
    {
        if( urlString == null )
        {
            return;
        }
        final Connection connection = getSqlConnection();
        try
        {
            final ScriptRunner runner = new ScriptRunner( connection, true, true );

            final URL url = new URL( urlString );
            final InputStreamReader inputStreamReader = new InputStreamReader( url.openStream() );
            final BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
            runner.runScript( bufferedReader );
        }
        finally
        {
            closeConnection( connection );
        }
    }

    private void closeConnection( final Connection connection )
    {
        if( connection == null )
        {
            return;
        }
        try
        {
            connection.close();
        }
        catch( SQLException sqle )
        {
            // ignore
        }
    }

    /**
     * Get the sql connection.
     *
     * @return The sql connection.
     * @throws SQLException Thrown if sql connection failed.
     * @since 0.1.0
     */
    private Connection getSqlConnection()
        throws SQLException
    {
        final String dbURL = dBInitializerInfo.dbUrl().get();
        final Property<Properties> connectionPropertiesProperty = dBInitializerInfo.connectionProperties();
        final Properties dbProperties = connectionPropertiesProperty.get();
        return DriverManager.getConnection( dbURL, dbProperties );
    }
}
