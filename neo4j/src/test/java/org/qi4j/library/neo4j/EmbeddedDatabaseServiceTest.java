package org.qi4j.library.neo4j;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class EmbeddedDatabaseServiceTest
    extends AbstractQi4jTest
{
    enum TestRelationships implements RelationshipType
    {
        KNOWS
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( FileConfiguration.class );
        module.services( EmbeddedDatabaseService.class );
    }

    @Test
    public void testDatabase()
    {
        GraphDatabaseService database = module.findService( EmbeddedDatabaseService.class ).get().database();

        {
            Transaction tx = database.beginTx();

            try
            {
                Node rickard = database.createNode();
                rickard.setProperty( "name", "Rickard" );

                Node niclas = database.createNode();
                niclas.setProperty( "name", "Niclas" );

                rickard.createRelationshipTo( niclas, TestRelationships.KNOWS );

                tx.success();
            }
            finally
            {
                tx.finish();
            }
        }
    }
}
