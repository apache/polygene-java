package org.qi4j.entitystore.sql.bootstrap;

import org.qi4j.entitystore.sql.database.DatabaseDerbyMixin;
import org.qi4j.entitystore.sql.database.DatabaseService;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class DerbySQLEntityStoreAssembler
        implements Assembler
{

    private final Visibility visibility;

    public DerbySQLEntityStoreAssembler()
    {
        this( Visibility.module );
    }

    public DerbySQLEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @SuppressWarnings( { "unchecked" } )
    public void assemble( ModuleAssembly ma )
            throws AssemblyException
    {
        ma.addServices( DatabaseService.class ).
                withMixins( DatabaseDerbyMixin.class ).
                identifiedBy( "entitystore-sql-derby" ).
                visibleIn( Visibility.module );
        ma.addServices( SQLEntityStoreService.class ).
                visibleIn( visibility ).
                instantiateOnStartup();
        ma.addServices( UuidIdentityGeneratorService.class ).
                visibleIn( visibility );
    }

}
