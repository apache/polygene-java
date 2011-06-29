/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

package org.qi4j.library.sql.ds;

import org.postgresql.ds.PGPoolingDataSource;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;

import javax.sql.DataSource;

/**
 * 
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public class PGSQLDataSourceServiceMixin
    implements DataSourceService, Activatable
{

    @This
    private Configuration<PGDataSourceConfiguration> _configuration;

    private PGPoolingDataSource _dataSource;

    public void activate()
        throws Exception
    {
        PGDataSourceConfiguration config = this._configuration.configuration();

        this._dataSource = new PGPoolingDataSource();

        this._dataSource.setServerName( config.server().get() );
        this._dataSource.setPortNumber( config.port().get() );
        this._dataSource.setDatabaseName( config.database().get() );
        this._dataSource.setUser( config.user().get() );
        this._dataSource.setPassword( config.password().get() );
    }

    public void passivate()
        throws Exception
    {
    }

    public DataSource getDataSource()
    {
        return this._dataSource;
    }
}
