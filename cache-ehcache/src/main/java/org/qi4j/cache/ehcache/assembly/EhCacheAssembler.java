package org.qi4j.cache.ehcache.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.cache.ehcache.EhCacheConfiguration;
import org.qi4j.cache.ehcache.EhCachePoolService;

public class EhCacheAssembler
    implements Assembler
{
    private Visibility visibility;

    public EhCacheAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( EhCachePoolService.class ).visibleIn( visibility );
        module.entities( EhCacheConfiguration.class );
    }
}
