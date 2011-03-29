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

import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.sql.assembly.DerbySQLEntityStoreAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.ds.DBCPBasicDataSourceServiceMixin;
import org.qi4j.library.sql.ds.DBCPDataSourceConfiguration;
import org.qi4j.library.sql.ds.DataSourceService;
import org.qi4j.library.sql.ds.DataSourceServiceComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public class DBCPBasicDataSourceServiceTest extends AbstractQi4jTest
{

    @SuppressWarnings("unchecked")
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( DataSourceServiceComposite.class ).withMixins( DBCPBasicDataSourceServiceMixin.class )
            .identifiedBy( DerbySQLEntityStoreAssembler.DATASOURCE_SERVICE_NAME ).instantiateOnStartup();

        ModuleAssembly config = module.layer().module( "config" );
        config.services( MemoryEntityStoreService.class );
        config.entities( DBCPDataSourceConfiguration.class, SQLConfiguration.class ).visibleIn( Visibility.layer );
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
