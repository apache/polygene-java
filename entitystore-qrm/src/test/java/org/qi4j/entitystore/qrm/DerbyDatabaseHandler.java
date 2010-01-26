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
package org.qi4j.entitystore.qrm;

import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.derby.drda.NetworkServerControl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.qrm.dbInitializer.DBInitializerConfiguration;
import org.qi4j.entitystore.qrm.test.TestProperty;

import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.sql.DriverManager.*;
import static org.junit.Assert.*;

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
    private static final int PING_COUNT = 10;
    private static final int PING_SLEEP_MILLIS = 50;
    private final NetworkServerControl nsc;

    public DerbyDatabaseHandler()
    {
        try
        {
            System.setProperty( "derby.authentication.provider", "BUILTIN" );
            System.setProperty( "derby.drda.securityMechanism", "CLEAR_TEXT_PASSWORD_SECURITY" );
            nsc = startServer();
            waitForStart();
            initDriver();
            initSchema();
        }
        catch( Exception e )
        {
            throw new RuntimeException( "Error initializing Derby", e );
        }
    }

    private Properties createConnectionProperties()
    {
        final Properties dbProperties = new Properties();
        dbProperties.setProperty( "username", DERBY_USER );
        dbProperties.setProperty( "password", DERBY_PASSWORD );
        return dbProperties;
    }

    public void initDbInitializerInfo( final ModuleAssembly module )
    {
        initDbInitializerInfo( module, SCHEMA_FILE, DATA_FILE );
    }

    public void initDbInitializerInfo( final ModuleAssembly module, final String schemaFile, final String dataFile )
    {
//        final DBInitializerConfiguration configuration = module.on( DBInitializerConfiguration.class ).to();
//        configuration.dbUrl().set( JDBC_URL );
//        configuration.connectionProperties().set( createConnectionProperties() );
//        if( schemaFile != null )
//        {
//            configuration.schemaUrl().set( getUrlString( schemaFile ) );
//        }
//        if( dataFile != null )
//        {
//            configuration.dataUrl().set( getUrlString( dataFile ) );
//        }
    }

    public String getUrlString( final String file )
    {
        final URL url = getClass().getResource( file );
        assertNotNull( "If run inside ide, make sure file " + file + " is part of project resources.", url );
        return url.toString();
    }

    private void initSchema()
        throws SQLException
    {
        final Connection connection = getJDBCConnection();
        try
        {
            final Statement statement = connection.createStatement();
            // executeIgnore( statement, "DROP SCHEMA SA" );
            executeIgnore( statement, "CREATE SCHEMA SA" );
            removeTables( statement );
        }
        finally
        {
            if( connection != null )
            {
                connection.close();
            }
        }
    }

    private void removeTables( final Statement statement )
        throws SQLException
    {
        // Ensure that the all test tables are removed.
        final DatabaseMetaData data = statement.getConnection().getMetaData();
        final ResultSet tables = data.getTables( null, null, null, new String[]{ "TABLE" } );
        while( tables.next() )
        {
            executeIgnore( statement, "DROP TABLE " + tables.getString( "TABLE_NAME" ) );
        }
    }

    private void executeIgnore( final Statement statement, final String sql )
    {
        try
        {
            statement.execute( sql );
        }
        catch( SQLException e )
        {
            System.err.println( "Error executing statement: " + sql + " " + e.getMessage() );
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

    private NetworkServerControl startServer()
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
            assertEquals( "There must be 2 rows in persons table.", 2, numberOfRows );
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
     *
     * @throws java.sql.SQLException Thrown if initializing connection failed.
     * @since 0.1.0
     */
    public final Connection getJDBCConnection()
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
    private void waitForStart()
        throws Exception
    {
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
        shutdown();
        fail( "DB is not started after waiting for [" + PING_COUNT * PING_SLEEP_MILLIS + "] ms" );
    }

    public void shutdown()
    {
        if( nsc != null )
        {
            try
            {
                nsc.shutdown();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    public DBInitializerConfiguration createDbInitializerConfigMock()
    {
        final Mockery context = new Mockery();
        final DBInitializerConfiguration info = context.mock( DBInitializerConfiguration.class );
        context.checking(
            new Expectations()
            {
                {
                    allowing( info ).connectionProperties();
                    will( returnValue( createProperty( "connectionProperties", createConnectionProperties() ) ) );
                    allowing( info ).schemaUrl();
                    will( returnValue( createProperty( "schemaUrl", getUrlString( SCHEMA_FILE ) ) ) );
                    allowing( info ).dataUrl();
                    will( returnValue( createProperty( "dataUrl", getUrlString( DATA_FILE ) ) ) );
                    allowing( info ).dbUrl();
                    will( returnValue( createProperty( "dbUrl", JDBC_URL ) ) );
                }
            } );
        return info;
    }

    private <T> Property<T> createProperty( final String name, final T value )
    {
        return new TestProperty<T>( value, QualifiedName.fromClass( DBInitializerConfiguration.class, name ) );
    }

    public int executeStatement( final String sql, final ResultSetCallback callback )
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try
        {
            connection = getJDBCConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery( sql );
            int count = 0;
            while( rs.next() )
            {
                callback.row( rs );
                count++;
            }
            return count;
        }
        catch( SQLException sqle )
        {
            throw new RuntimeException( "Error executing statement: " + sql + " with callback " + callback, sqle );
        }
        finally
        {
            closeIt( rs );
            closeIt( statement );
            closeIt( connection );
        }
    }

    public int executeUpdate( final String sql )
    {
        Connection connection = null;
        Statement statement = null;
        int result = -1;
        try
        {
            connection = getJDBCConnection();
            statement = connection.createStatement();
            result = statement.executeUpdate( sql );
            return result;
        }
        catch( SQLException sqle )
        {
            throw new RuntimeException( "Error executing update: " + sql, sqle );
        }
        finally
        {
            closeIt( statement );
            closeIt( connection );
        }
    }

    private void closeIt( final Object jdbcHandle )
    {
        if( jdbcHandle == null )
        {
            return;
        }
        try
        {
            if( jdbcHandle instanceof ResultSet )
            {
                ( (ResultSet) jdbcHandle ).close();
                return;
            }
            if( jdbcHandle instanceof Statement )
            {
                ( (Statement) jdbcHandle ).close();
                return;
            }
            if( jdbcHandle instanceof Connection )
            {
                ( (Connection) jdbcHandle ).close();
            }
        }
        catch( SQLException e )
        {
            System.err.print( "Error closing " + jdbcHandle.getClass() + ": " + e.getMessage() );
        }
    }

    /**
     * @autor Michael Hunger
     * @since 19.05.2008
     */
    public static interface ResultSetCallback
    {
        void row( ResultSet rs )
            throws SQLException;
    }
}