package org.apache.polygene.entitystore.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.jooq.SQLDialect;

class TearDown
{
    static void dropTables( Module module, SQLDialect dialect, Runnable after )
    {
        if( module == null ){
            System.err.println( "WARNING: 'module' was null. Happens if there was a pre-activation error. Otherwise an InternalError" );
        }
        UnitOfWorkFactory unitOfWorkFactory = module.unitOfWorkFactory();
        ServiceFinder serviceFinder = module.serviceFinder();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "Cleaning up. Drop Tables" ) ) )
        {
            try( Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection() )
            {
                connection.setAutoCommit( true );
                try( Statement stmt = connection.createStatement() )
                {
                    dropTable( dialect, stmt, "TYPES" );
                    dropTable( dialect, stmt, "ENTITIES" );
                    dropTable( dialect, stmt, "TestEntity" );
                    dropTable( dialect, stmt, "TestEntity_ASSOCS" );
                    dropIndex( dialect, stmt, "TestEntity_ASSOCS" );
                    dropTable( dialect, stmt, "PhoneNumber" );
                    dropTable( dialect, stmt, "PhoneNumber_ASSOCS" );
                    dropIndex( dialect, stmt, "PhoneNumber_ASSOCS" );
                    dropTable( dialect, stmt, "Person" );
                    dropTable( dialect, stmt, "Person_ASSOCS" );
                    dropIndex( dialect, stmt, "Person_ASSOCS" );
                    dropTable( dialect, stmt, "City" );
                    dropTable( dialect, stmt, "City_ASSOCS" );
                    dropIndex( dialect, stmt, "City_ASSOCS" );
                    dropTable( dialect, stmt, "Address" );
                    dropTable( dialect, stmt, "Address_ASSOCS" );
                    dropIndex( dialect, stmt, "Address_ASSOCS" );
                    dropTable( dialect, stmt, "Country" );
                    dropTable( dialect, stmt, "Country_ASSOCS" );
                    dropIndex( dialect, stmt, "Country_ASSOCS" );
                }
            }
        }
        catch( SQLException e )
        {
            throw new RuntimeException( "Unable to clean up tables.", e );
        }
        finally
        {
            after.run();
        }
    }

    private static void dropTable( SQLDialect dialect, Statement stmt, String tableName )
    {
        try
        {
            if( dialect == SQLDialect.MYSQL || dialect == SQLDialect.MARIADB )
            {
                stmt.execute( String.format( "DROP TABLE `%s`", tableName ) );
            }
            else
            {
                stmt.execute( String.format( "DROP TABLE \"%s\"", tableName ) );
            }
        }
        catch( SQLException e )
        {
            //  ignore. Not all tables will be present in all tests.
        }
    }

    private static void dropIndex( SQLDialect dialect, Statement stmt, String tableName )
    {
        try
        {
            if( dialect == SQLDialect.MYSQL || dialect == SQLDialect.MARIADB )
            {
                stmt.execute( String.format( "DROP INDEX `IDX_%s`", tableName ) );
            }
            else
            {
                stmt.execute( String.format( "DROP INDEX \"IDX_%s\"", tableName ) );
            }
        }
        catch( SQLException e )
        {
            //  ignore. Not all tables will be present in all tests.
        }
    }
}
