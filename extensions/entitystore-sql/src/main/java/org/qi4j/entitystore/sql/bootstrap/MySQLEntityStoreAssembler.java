package org.qi4j.entitystore.sql.bootstrap;

import org.qi4j.entitystore.sql.database.DatabaseMySQLMixin;
import org.qi4j.entitystore.sql.database.DatabaseService;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class MySQLEntityStoreAssembler
        implements Assembler
{

    private final Visibility visibility;

    public MySQLEntityStoreAssembler()
    {
        this( Visibility.module );
    }

    public MySQLEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly ma )
            throws AssemblyException
    {
        ma.addServices( DatabaseService.class ).
                withMixins( DatabaseMySQLMixin.class ).
                identifiedBy( "entitystore-sql-mysql" ).
                visibleIn( Visibility.module );
        ma.addServices( SQLEntityStoreService.class ).
                visibleIn( visibility ).
                instantiateOnStartup();
        ma.addServices( UuidIdentityGeneratorService.class ).
                visibleIn( visibility );
    }

}
