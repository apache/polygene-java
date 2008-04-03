package org.qi4j.entity.jdbm;

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.entity.AbstractEntityStorePerformanceTest;

/**
 * Performance test for JdbmEntityStoreComposite
 */
public class JdbmEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( JdbmEntityStoreComposite.class );
    }

    @Override @Test
    public void whenNewEntitiesThenPerformanceIsOk() throws Exception
    {
        super.whenNewEntitiesThenPerformanceIsOk();
    }

    @Override @Test
    public void whenBulkNewEntitiesThenPerformanceIsOk() throws Exception
    {
        super.whenBulkNewEntitiesThenPerformanceIsOk();
    }

    @Override @Test
    public void whenFindEntityThenPerformanceIsOk() throws Exception
    {
        super.whenFindEntityThenPerformanceIsOk();
    }
}