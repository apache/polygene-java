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
package org.qi4j.entity.ibatis;

import java.io.PrintWriter;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import java.net.URL;
import java.sql.Connection;
import static java.sql.DriverManager.getConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.derby.drda.NetworkServerControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public class DerbyDatabaseHandler
{
    private static final String JDBC_URL = "jdbc:derby://localhost/testdb;create=true";
    private static final String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";
    private static final String DERBY_USER = "sa";
    private static final String DERBY_PASSWORD = "derbypass";

    private static final String SCHEMA_FILE = "testDbSchema.sql";
    private static final String DATA_FILE = "testDbData.sql";
    private static final int PING_COUNT = 2;
    private static final int PING_SLEEP_MILLIS = 200;

    protected DerbyDatabaseHandler() 
    {
        try {
        final Properties systemProperties = System.getProperties();
        systemProperties.setProperty( "derby.drda.securityMechanism", "CLEAR_TEXT_PASSWORD_SECURITY" );
            initSchema();
            waitUntilDerbyStarted();
        } catch(Exception e) {
            throw new RuntimeException( "Error initializing Derby",e);
        }
    }

    protected final DBInitializerInfo newDbInitializerInfo() {
        return newDbInitializerInfo( SCHEMA_FILE, DATA_FILE );
    }
    /**
     * Construct a new db initializer info.
     *
     * @return a new db initializer info.
     * @since 0.1.0
     * @param schemaFile
     * @param dataFile
     */
    protected final DBInitializerInfo newDbInitializerInfo( final String schemaFile, final String dataFile )
    {
        final Properties dbProperties = new Properties();
        dbProperties.setProperty( "username", DERBY_USER );
        dbProperties.setProperty( "password", DERBY_PASSWORD );
        return new DBInitializerInfo( JDBC_URL, dbProperties, getUrlString( schemaFile ), getUrlString( dataFile ));
    }

    public String getUrlString( final String file )
    {
        final URL url = getClass().getResource( file );
        assertNotNull( "If run inside ide, make sure file "+file+" is part of project resources.", url );
        return url.toString();
    }

    private void initSchema()
        throws SQLException
    {
        final Connection connection = getJDBCConnection();
        try
        {
            final Statement statement = connection.createStatement();
            statement.execute( "DROP SCHEMA SA" );
            statement.execute( "CREATE SCHEMA SA" );

//            // Ensure that the all test tables are removed.
//
//            final DatabaseMetaData data = connection.getMetaData();
//            final ResultSet tables = data.getTables( null, null, null, new String[]{ "TABLE" } );
//            while( tables.next() )
//            {
//                final String tableName = tables.getString( "TABLE_NAME" );
//                try
//                {
//                    statement.execute( "DROP TABLE " + tableName );
//                }
//                catch( SQLException e )
//                {
//                    // Ignore
//                }
//            }
        }
        finally
        {
            if( connection != null )
            {
                connection.close();
            }
        }
    }

    private void initDriver()
    {
        // Initialize derby driver.
        try
        {
            Class.forName( DERBY_DRIVER_CLASS_NAME );
        }
        catch( ClassNotFoundException e )
        {
            fail( "Derby client artifact must be included to run this test." );
        }
    }

    private NetworkServerControl createNetworkServerControl()
        throws Exception
    {
        final NetworkServerControl nsc = new NetworkServerControl();
        final PrintWriter logOuput = new PrintWriter( out );
        nsc.start( logOuput );
        return nsc;
    }
    
    /**
     * Check data initialization.
     *
     * @throws java.sql.SQLException Thrown if closing connection failed.
     * @since 0.1.0
     */
    public final void checkDataInitialization()
        throws SQLException
    {
        // Validate that the db is initialized
        Connection connection = null;
        try
        {
            connection = getJDBCConnection();
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery( "SELECT COUNT(*) FROM PERSON" );
            assertTrue( resultSet.next() );
            final int numberOfRows = resultSet.getInt( 1 );
            assertEquals( "There must be 2 rows in persons table.", PING_COUNT, numberOfRows );
        }
        finally
        {
            if( connection != null )
            {
                connection.close();
            }
        }
    }

    /**
     * Returns the jdbc connection to test db. Must not return {@code null}.
     *
     * @return The jdbc connection to test db.
     * @throws java.sql.SQLException Thrown if initializing connection failed.
     * @since 0.1.0
     */
    final Connection getJDBCConnection()
        throws SQLException
    {
        return getConnection( JDBC_URL, DERBY_USER, DERBY_PASSWORD );
    }


    /**
     * Wait until derby started.
     *
     * @throws InterruptedException Thrown if sleep fails.
     * @since 0.1.0
     */
    private void waitUntilDerbyStarted()
        throws Exception
    {
        initDriver();
        NetworkServerControl nsc = null;
        try
        {
            nsc = createNetworkServerControl();

            for( int count = 0; count < PING_COUNT; count++ )
            {
                try
                {
                    nsc.ping();
                    return;
                }
                catch( Exception e )
                {
                    sleep( PING_SLEEP_MILLIS );
                }
            }
            fail( "DB is not started after waiting for [400] ms" );
        }
        finally
        {
            if( nsc != null )
            {
                nsc.shutdown();
            }
        }
    }
}