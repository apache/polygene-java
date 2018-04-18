/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.index.sql.postgresql;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.indexing.AbstractComplexQueryTest;
import org.junit.jupiter.api.BeforeEach;

@Docker( image = "org.apache.polygene:org.apache.polygene.internal.docker-postgres",
         ports = @Port( exposed = 8801, inner = 5432),
         waitFor = @WaitFor( value = "PostgreSQL init process complete; ready for start up.", timeoutInMillis = 30000),
         newForEachCase = false
)
public class PostgreSQLComplexQueryTest
    extends AbstractComplexQueryTest
{

    @Override
    public void assemble( ModuleAssembly mainModule )
        throws AssemblyException
    {
        SQLTestHelper.sleep();
        super.assemble( mainModule );
        String host = "localhost";
        int port = 8801;
        SQLTestHelper.assembleWithMemoryEntityStore( mainModule, host, port );
    }

    @Override
    @BeforeEach
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
                SQLTestHelper.setUpTest( this.serviceFinder );
            }

            // If we got this far, the database must have been available, and exception must have
            // had other reason!
            throw e;
        }
    }
}
