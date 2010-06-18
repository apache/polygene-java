package org.qi4j.entitystore.sql.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLEntityStoreService;
import org.qi4j.entitystore.sql.database.DatabasePostgreSQLMixin;
import org.qi4j.entitystore.sql.database.DatabaseService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class PostgreSQLEntityStoreAssembler
        implements Assembler
{

    private final Visibility visibility;

    public PostgreSQLEntityStoreAssembler()
    {
        this( Visibility.module );
    }

    public PostgreSQLEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly ma )
            throws AssemblyException
    {
        ma.addServices( DatabaseService.class ).
                withMixins( DatabasePostgreSQLMixin.class ).
                identifiedBy( "entitystore-sql-postgre" ).
                visibleIn( Visibility.module );
        ma.addServices( SQLEntityStoreService.class ).
                visibleIn( visibility ).
                instantiateOnStartup();
        ma.addServices( UuidIdentityGeneratorService.class ).
                visibleIn( visibility );
    }

}
