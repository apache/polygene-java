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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;

import org.apache.commons.dbcp.BasicDataSource;

@Mixins( DBCPDataSourceServiceImporter.Mixin.class )
public interface DBCPDataSourceServiceImporter
        extends ServiceImporter, Activatable, ServiceComposite
{

    class Mixin
            extends AbstractDataSourceServiceImporterMixin<BasicDataSource>
            implements Activatable, ServiceImporter
    {

        @Override
        protected BasicDataSource setupDataSourcePool( DataSourceConfigurationValue config )
                throws ClassNotFoundException
        {
            BasicDataSource pool = new BasicDataSource();

            Class.forName( config.driver().get() );
            pool.setDriverClassName( config.driver().get() );
            pool.setUrl( config.url().get() );

            if ( !config.username().get().equals( "" ) ) {
                pool.setUsername( config.username().get() );
                pool.setPassword( config.password().get() );
            }

            return pool;
        }

        @Override
        protected void passivateDataSourcePool( BasicDataSource dataSourcePool )
                throws Exception
        {
            dataSourcePool.close();
        }

    }

}
