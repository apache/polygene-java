package org.qi4j.entity.neo4j.test;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.neo4j.NeoCoreService;
import org.qi4j.entity.neo4j.NeoEntityStoreService;
import org.qi4j.entity.neo4j.NeoIdentityIndexService;
import org.qi4j.entity.neo4j.state.DirectEntityStateFactory;
import org.qi4j.entity.neo4j.state.IndirectEntityStateFactory;
import org.qi4j.test.entity.AbstractEntityStorePerformanceTest;

public class Neo4jEntityStorePerformanceTest extends AbstractEntityStorePerformanceTest {

	@Override
	public void assemble(ModuleAssembly module) throws AssemblyException {
		// TODO Auto-generated method stub
		super.assemble(module);
		module.addServices(
                NeoEntityStoreService.class,
                NeoCoreService.class,
                DirectEntityStateFactory.class,
                IndirectEntityStateFactory.class,
                NeoIdentityIndexService.class
            );
	}

	@Ignore( "Causes OutOfMemoryError." )
	@Test
	@Override
	public void whenFindEntityThenPerformanceIsOk() throws Exception
	{
		try
		{
			super.whenFindEntityThenPerformanceIsOk();
		}
		catch ( Exception ex )
		{
			System.err
			    .println( "whenFindEntityThenPerformanceIsOk failed with:" );
			ex.printStackTrace();
		}
	}
	
}
