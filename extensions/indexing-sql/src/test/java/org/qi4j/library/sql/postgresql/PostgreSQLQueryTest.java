/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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

package org.qi4j.library.sql.postgresql;

import org.junit.Ignore;
import org.junit.Test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.indexing.AbstractQueryTest;

@Ignore // DO NOT PASS YET
public class PostgreSQLQueryTest extends AbstractQueryTest
{
    @Override
    public void assemble( ModuleAssembly mainModule )
        throws AssemblyException
    {
        super.assemble( mainModule );
        SQLTestHelper.assembleWithMemoryEntityStore( mainModule );
    }

    @Override
    public void setUp()
        throws Exception
    {
        try
        {
            super.setUp();
        }
        catch( Exception e )
        {
            // Let's check if exception was because database was not available
            if( this.module != null )
            {
                SQLTestHelper.setUpTest( this.module );
            }

            // If we got this far, the database must have been available, and exception must have had other reason!
            throw e;
        }
    }

    @Override
    public void tearDown()
        throws Exception
    {
        SQLTestHelper.tearDownTest( module, getLog() );
        super.tearDown();
    }

    @Test
    public void ignoredScript29()
    {
        // PostgreSQL indexing engine supports complex value indexing.
        super.script29();
    }

    @Test
    public void ignoredScript23()
        throws Exception
    {
        // This test doesn't seem to sporadically fail anymore, at least for this test.
        super.script23();
    }
}
