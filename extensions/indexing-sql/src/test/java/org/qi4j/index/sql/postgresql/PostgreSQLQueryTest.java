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
package org.qi4j.index.sql.postgresql;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.indexing.AbstractQueryTest;

import static org.qi4j.test.util.Assume.assumeConnectivity;

/**
 * PostgreSQL Query Tests.
 * <p>Many features are not supported.</p>
 */
// See org.qi4j.index.sql.support.skeletons.SQLCompatEntityStateWrapper that filter out unsupported properties.
public class PostgreSQLQueryTest
    extends AbstractQueryTest
{
    @BeforeClass
    public static void beforePostgreSQLQueryTests()
    {
        assumeConnectivity( "localhost", 5432 );
    }

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

            // If we got this far, the database must have been available, and exception must have
            // had other reason!
            throw e;
        }
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script04_ne()
        throws EntityFinderException
    {
        super.script04_ne();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script12_ne()
        throws EntityFinderException
    {
        super.script04_ne();
    }

    @Test
    @Ignore( "NamedAssociation are not supported by SQL Indexing" )
    @Override
    public void script35()
    {
        super.script35();
    }

    @Test
    @Ignore( "NamedAssociation are not supported by SQL Indexing" )
    @Override
    public void script36()
    {
        super.script36();
    }

    @Test
    @Ignore( "Queries on Enums are not supported by SQL Indexing" )
    @Override
    public void script38()
    {
        super.script38();
    }

    @Test
    @Ignore( "Queries on Enums and NeSpecification are not supported by SQL Indexing" )
    @Override
    public void script39()
    {
        super.script39();
    }

    @Test
    @Ignore( "Date is not supported by SQL Indexing" )
    @Override
    public void script40_Date()
    {
        super.script40_Date();
    }

    @Test
    @Ignore( "DateTime is not supported by SQL Indexing" )
    @Override
    public void script40_DateTime()
    {
        super.script40_DateTime();
    }

    @Test
    @Ignore( "LocalDate is not supported by SQL Indexing" )
    @Override
    public void script40_LocalDate()
    {
        super.script40_LocalDate();
    }

    @Test
    @Ignore( "LocalDateTime is not supported by SQL Indexing" )
    @Override
    public void script40_LocalDateTime()
    {
        super.script40_LocalDateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_Date()
    {
        super.script41_Date();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_DateTime()
    {
        super.script41_DateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_LocalDate()
    {
        super.script41_LocalDate();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script41_LocalDateTime()
    {
        super.script41_LocalDateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_Date()
    {
        super.script42_Date();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_DateTime()
    {
        super.script42_DateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_LocalDate()
    {
        super.script42_LocalDate();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script42_LocalDateTime()
    {
        super.script42_LocalDateTime();
    }

    @Test
    @Ignore( "Date is not supported by SQL Indexing" )
    @Override
    public void script43_Date()
    {
        super.script43_Date();
    }

    @Test
    @Ignore( "DateTime is not supported by SQL Indexing" )
    @Override
    public void script43_DateTime()
    {
        super.script43_DateTime();
    }

    @Test
    @Ignore( "LocalDate is not supported by SQL Indexing" )
    @Override
    public void script43_LocalDate()
    {
        super.script43_LocalDate();
    }

    @Test
    @Ignore( "LocalDateTime is not supported by SQL Indexing" )
    @Override
    public void script43_LocalDateTime()
    {
        super.script43_LocalDateTime();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script51_BigInteger()
    {
        super.script51_BigInteger();
    }

    @Test
    @Ignore( "NeSpecification is not supported by SQL Indexing" )
    @Override
    public void script51_BigDecimal()
    {
        super.script51_BigDecimal();
    }
}
