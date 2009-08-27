package org.qi4j.entitystore.neo4j;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.neo4j.state.DirectEntityStateFactory;
import org.qi4j.entitystore.neo4j.state.IndirectEntityStateFactory;

public enum Configuration implements Assembler
{
    DIRECT
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices(
                    NeoEntityStoreService.class,
                    NeoCoreService.class,
                    DirectEntityStateFactory.class,
                    NeoIdentityIndexService.class
                );
            }
        },

    INDIRECT
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                DIRECT.assemble( module );
                module.addServices( IndirectEntityStateFactory.class );
            }
        },

    IDENTITY_GENERATOR
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( NeoIdentityGeneratorService.class );
            }
        },
    INDIRECT_AND_IDENTITY_GENERATOR
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                INDIRECT.assemble( module );
                IDENTITY_GENERATOR.assemble( module );
            }
        };

    public abstract void assemble( ModuleAssembly module ) throws AssemblyException;
}
