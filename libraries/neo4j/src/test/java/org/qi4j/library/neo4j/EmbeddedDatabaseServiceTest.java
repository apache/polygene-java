/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.neo4j;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.fileconfig.FileConfigurationService;
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
        module.services( FileConfigurationService.class );
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
