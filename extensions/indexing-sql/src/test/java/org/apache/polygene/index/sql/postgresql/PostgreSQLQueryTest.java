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
import org.apache.polygene.test.indexing.AbstractQueryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * PostgreSQL Query Tests.
 * <p>Many features are not supported.</p>
 */
// See org.apache.polygene.index.sql.support.skeletons.SQLCompatEntityStateWrapper that filter out unsupported properties.
@Docker( image = "org.apache.polygene:org.apache.polygene.internal.docker-postgres",
         ports = @Port( exposed = 8801, inner = 5432),
         waitFor = @WaitFor( value = "PostgreSQL init process complete; ready for start up.", timeoutInMillis = 30000),
         newForEachCase = false
)
public class PostgreSQLQueryTest
    extends AbstractQueryTest
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

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script04_ne()
    {
        super.script04_ne();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script12_ne()
    {
        super.script04_ne();
    }

    @Test
    @Disabled( "NamedAssociation are not supported by SQL Indexing" )
    @Override
    public void script35()
    {
        super.script35();
    }

    @Test
    @Disabled( "NamedAssociation are not supported by SQL Indexing" )
    @Override
    public void script36()
    {
        super.script36();
    }

    @Test
    @Disabled( "Queries on Enums are not supported by SQL Indexing" )
    @Override
    public void script38()
    {
        super.script38();
    }

    @Test
    @Disabled( "Queries on Enums and NeSpecification are not supported by SQL Indexing" )
    @Override
    public void script39()
    {
        super.script39();
    }

    @Test
    @Disabled( "Date is not supported by SQL Indexing" )
    @Override
    public void script40_Date()
    {
        super.script40_Date();
    }

    @Test
    @Disabled( "DateTime is not supported by SQL Indexing" )
    @Override
    public void script40_DateTime()
    {
        super.script40_DateTime();
    }

    @Test
    @Disabled( "LocalDate is not supported by SQL Indexing" )
    @Override
    public void script40_LocalDate()
    {
        super.script40_LocalDate();
    }

    @Test
    @Disabled( "LocalDateTime is not supported by SQL Indexing" )
    @Override
    public void script40_LocalDateTime()
    {
        super.script40_LocalDateTime();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_Instant()
    {
        super.script41_Instant();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_DateTime()
    {
        super.script41_DateTime();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_LocalDate()
    {
        super.script41_LocalDate();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_LocalDateTime()
    {
        super.script41_LocalDateTime();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_Instant()
    {
        super.script42_Instant();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_DateTime()
    {
        super.script42_DateTime();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_LocalDate()
    {
        super.script42_LocalDate();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_LocalDateTime()
    {
        super.script42_LocalDateTime();
    }

    @Test
    @Disabled( "Date is not supported by SQL Indexing" )
    @Override
    public void script43_Date()
    {
        super.script43_Date();
    }

    @Test
    @Disabled( "DateTime is not supported by SQL Indexing" )
    @Override
    public void script43_DateTime()
    {
        super.script43_DateTime();
    }

    @Test
    @Disabled( "LocalDate is not supported by SQL Indexing" )
    @Override
    public void script43_LocalDate()
    {
        super.script43_LocalDate();
    }

    @Test
    @Disabled( "LocalDateTime is not supported by SQL Indexing" )
    @Override
    public void script43_LocalDateTime()
    {
        super.script43_LocalDateTime();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script51_BigInteger()
    {
        super.script51_BigInteger();
    }

    @Test
    @Disabled( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script51_BigDecimal()
    {
        super.script51_BigDecimal();
    }
}
