package org.qi4j.sample.spatial.domain.openstreetmap.model.test;

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.sample.spatial.domain.openstreetmap.model.assembly.OpenStreetMapDomainModelAssembler;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Created by jj on 28.11.14.
 */
public class TestOpenStreetMapModel extends AbstractQi4jTest {

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new OpenStreetMapDomainModelAssembler().assemble(module);
    }

    @Test
    public void foo()
    {
        System.out.println("foo");
    }
}
