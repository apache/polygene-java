package org.apache.polygene.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.entitystore.sql.assembly.AbstractSQLEntityStoreAssembler;
import org.apache.polygene.library.sql.common.SQLConfiguration;

import static org.apache.polygene.api.usecase.UsecaseBuilder.newUsecase;

public class TearDownUtil
{

    public static void dropSchema( Module storageModule, String testName )
        throws Exception
    {
        String usecaseName = "Delete " + testName + " test data";
        UnitOfWorkFactory uowf = storageModule.unitOfWorkFactory();
        ServiceFinder serviceFinder = storageModule.serviceFinder();
        UnitOfWork uow = uowf.newUnitOfWork( newUsecase( usecaseName ) );
        try
        {
            SQLConfiguration config = uow.get( SQLConfiguration.class, AbstractSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
            Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection();
            connection.setAutoCommit( false );
            String schemaName = config.schemaName().get();
            try( Statement stmt = connection.createStatement() )
            {
                stmt.execute( String.format( "DROP SCHEMA \"%s\" CASCADE", schemaName ) );
                connection.commit();
            }
        }
        finally
        {
            uow.discard();
        }

    }
}
