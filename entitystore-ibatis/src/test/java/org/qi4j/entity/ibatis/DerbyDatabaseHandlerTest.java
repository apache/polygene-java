package org.qi4j.entity.ibatis;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.entity.ibatis.DerbyDatabaseHandler;

/**
 * @autor Michael Hunger
 * @since 18.05.2008
 */
public class DerbyDatabaseHandlerTest
{
    private DerbyDatabaseHandler derbyDatabaseHandler;

    @Test
    public void testInitDerby() throws SQLException
    {
        final Connection connection = derbyDatabaseHandler.getJDBCConnection();
        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        assertNotNull( "DataBaseMetaData from initialized DB",databaseMetaData );
    }

    @After
    public void tearDown() throws Exception
    {
        if (derbyDatabaseHandler!=null)
            derbyDatabaseHandler.shutdown();

    }

    @Before
    public void setUp() throws Exception
    {
        derbyDatabaseHandler = new DerbyDatabaseHandler();
    }
}
