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
package org.qi4j.entitystore.qrm.dbInitializer;

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

/**
 * {@code DBIntializer} initialize the db.
 */
public final class DBInitializer
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Initialize the database.
     * Reads scripts from schemaURL and data scripts from dataUrl
     *
     * @param schemaUrl            The URL to where the DB Schema to be initialized into the DB resides.
     * @param dataUrl              The URL to where the Data to be initialized into the DB resides.
     * @param dbUrl                The URL to connect to the DB.
     * @param connectionProperties Properties to be used in the SQL Connection.
     *
     * @throws java.sql.SQLException Thrown if db initialization failed.
     * @throws java.io.IOException   Thrown if reading schema or data sql resources failed.
     * @since 0.1.0
     *        todo handle exceptions
     */
    public final void initialize( String schemaUrl, String dataUrl, String dbUrl, Properties connectionProperties )
        throws SQLException, IOException
    {
        Connection connection1 = getSqlConnection( dbUrl, connectionProperties );
        runScript( schemaUrl, connection1 );
        Connection connection2 = getSqlConnection( dbUrl, connectionProperties );
        runScript( dataUrl, connection2 );
    }

    private void runScript( final String urlString, Connection connection )
        throws SQLException, IOException
    {
        if( urlString == null )
        {
            return;
        }
        try
        {
            final ScriptRunner runner = new ScriptRunner( connection, true, true );

            final URL url = new URL( urlString );
            final InputStreamReader inputStreamReader = new InputStreamReader( url.openStream() );
            final BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
            runner.runScript( bufferedReader );
            bufferedReader.close();
            inputStreamReader.close();
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
     *
     * @throws SQLException Thrown if sql connection failed.
     * @since 0.1.0
     */
    private Connection getSqlConnection( String dbURL, Properties connectionProperties )
        throws SQLException
    {
        return DriverManager.getConnection( dbURL, connectionProperties );
    }
}
