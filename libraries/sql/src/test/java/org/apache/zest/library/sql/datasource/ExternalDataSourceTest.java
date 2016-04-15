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
package org.apache.zest.library.sql.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.sql.assembly.ExternalDataSourceAssembler;
import org.apache.zest.library.sql.common.SQLUtil;
import org.apache.zest.test.AbstractZestTest;

public class ExternalDataSourceTest
        extends AbstractZestTest
{

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        BasicDataSource externalDataSource = new BasicDataSource();
        externalDataSource.setDriverClassName( "org.apache.derby.jdbc.EmbeddedDriver" );
        externalDataSource.setUrl( "jdbc:derby:memory:testdbexternal;create=true" );
        // START SNIPPET: assembly
        new ExternalDataSourceAssembler( externalDataSource ).
                visibleIn( Visibility.module ).
                identifiedBy( "datasource-external-id" ).
                withCircuitBreaker( DataSources.newDataSourceCircuitBreaker() ).
                assemble( module );
        // END SNIPPET: assembly
    }

    @Test
    public void test()
            throws SQLException
    {
        DataSource dataSource = serviceFinder.findService( DataSource.class ).get();
        Connection connection = dataSource.getConnection();
        try {
            connection.getMetaData();
        } finally {
            SQLUtil.closeQuietly( connection );
        }
    }

}
