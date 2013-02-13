package org.qi4j.entitystore.leveldb;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * LevelDB EntityStore assembly.
 */
public class LevelDBEntityStoreAssembler
    implements Assembler
{

    private String identity;
    private Visibility visibility = Visibility.module;
    private ModuleAssembly configModule;
    private Visibility configVisibility = Visibility.module;

    public LevelDBEntityStoreAssembler identifiedBy( String identity )
    {
        this.identity = identity;
        return this;
    }

    public LevelDBEntityStoreAssembler visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public LevelDBEntityStoreAssembler withConfig( ModuleAssembly configModule, Visibility configVisibility )
    {
        this.configModule = configModule;
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        if( configModule == null )
        {
            configModule = module;
        }
        module.services( UuidIdentityGeneratorService.class );
        module.services( LevelDBEntityStoreService.class ).visibleIn( visibility );
        configModule.entities( LevelDBEntityStoreConfiguration.class ).visibleIn( configVisibility );
        if( identity != null )
        {
            module.services( LevelDBEntityStoreService.class ).identifiedBy( identity );
        }
    }
}
