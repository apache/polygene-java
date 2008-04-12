package org.qi4j.entity.memory;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * Test for MemoryEntityStoreComposite
 */
public class MemoryEntityStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( MemoryEntityStoreService.class );
    }
}
