package org.qi4j.entitystore.sql.database;

import java.sql.ResultSet;
import java.io.Reader;
import org.qi4j.entitystore.sql.util.SQLUtil;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import static org.qi4j.entitystore.sql.util.SQLUtil.*;

public abstract class DatabasePostgreSQLMixin
        extends AbstractDatabaseService
{

    private static final long serialVersionUID = 1L;

    private static final String CREATE_MAP_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " (" + IDENTITY_COLUMN + " CHAR(128) PRIMARY KEY, " + STATE_COLUMN + " VARCHAR(1048576))";

    public DatabasePostgreSQLMixin( @This Configuration<DatabaseConfiguration> cfg )
    {
        super( cfg );
    }

    @Override
    protected void initDatabase()
    {
        Connection connection = null;
        PreparedStatement createMapTable = null;
        try {
            connection = openConnection();
            if ( needSchemaCreation( connection ) ) {
                createMapTable = connection.prepareStatement( CREATE_MAP_TABLE_SQL );
                createMapTable.executeUpdate();
                connection.commit();
                System.out.println( "Database successfully initialized" );
            } else {
                System.out.println( "Existing database found" );
            }
        } catch ( SQLException ex ) {
            throw new RuntimeException( "Unable to initialize database", ex );
        } finally {
            closeQuietly( createMapTable );
            closeQuietly( connection );
        }
    }

    public Reader getEntityValue( ResultSet resultSet )
            throws SQLException
    {
        return resultSet.getCharacterStream( SQLUtil.STATE_COLUMN );
    }

}
