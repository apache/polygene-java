/*
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
package org.qi4j.library.sql.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.sql.assembly.ExternalDataSourceAssembler;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.test.AbstractQi4jTest;

import org.apache.commons.dbcp.BasicDataSource;

public class ExternalDataSourceTest
        extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        BasicDataSource externalDataSource = new BasicDataSource();
        externalDataSource.setDriverClassName( "org.apache.derby.jdbc.EmbeddedDriver" );
        externalDataSource.setUrl( "jdbc:derby:memory:testdbexternal;create=true" );
        new ExternalDataSourceAssembler( "datasource-external",
                                         Visibility.module,
                                         externalDataSource,
                                         DataSources.newDataSourceCircuitBreaker() ).assemble( module );
    }

    @Test
    public void test()
            throws SQLException
    {
        DataSource dataSource = module.findService( DataSource.class ).get();
        Connection connection = dataSource.getConnection();
        try {
            connection.getMetaData();
        } finally {
            SQLUtil.closeQuietly( connection );
        }
    }

}
