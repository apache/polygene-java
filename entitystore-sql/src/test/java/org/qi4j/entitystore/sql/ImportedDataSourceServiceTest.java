/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assume;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.assembly.DerbySQLEntityStoreAssembler;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.ds.DataSourceService;
import org.qi4j.library.sql.ds.assembly.DataSourceAssembler;
import org.qi4j.library.sql.ds.assembly.ImportableDataSourceService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public class ImportedDataSourceServiceTest extends AbstractQi4jTest
{

    private static final String CONNECTION_STRING = "jdbc:derby:build/qi4jdata;create=true";

    @SuppressWarnings("unchecked")
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl( CONNECTION_STRING );
        try
        {
            new DerbySQLEntityStoreAssembler( new DataSourceAssembler( new ImportableDataSourceService( dataSource ) ) )
            .assemble( module );
        } catch( AssemblyException e )
        {
            Assume.assumeNoException( e );
        }
    }

    @Test
    public void test()
        throws SQLException
    {
        DataSourceService dsService = serviceLocator.<DataSourceService> findService( DataSourceService.class ).get();
        Connection connection = null;
        try
        {
            connection = dsService.getDataSource().getConnection();
        }
        catch( SQLException ex )
        {
            SQLUtil.closeQuietly( connection );
            throw ex;
        }
    }

}
