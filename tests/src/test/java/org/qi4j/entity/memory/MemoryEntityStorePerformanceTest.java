package org.qi4j.entity.memory;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.entity.AbstractEntityStorePerformanceTest;

/**
 * Performance test for MemoryEntityStoreComposite
 */
public abstract class MemoryEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( MemoryEntityStoreService.class );
    }
}