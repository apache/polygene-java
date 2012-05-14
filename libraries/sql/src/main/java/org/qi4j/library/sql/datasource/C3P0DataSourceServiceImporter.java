/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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

import java.sql.SQLException;
import java.util.Properties;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * DataSource service implemented as a ServiceImporter.
 * 
 * Import visible DataSources as Services. The default Mixin use the C3P0
 * pooling system optionaly wrapped with CircuitBreaker using a proxy.
 */
@Mixins( C3P0DataSourceServiceImporter.Mixin.class )
public interface C3P0DataSourceServiceImporter
        extends ServiceImporter, Activatable, ServiceComposite
{

    class Mixin
            extends AbstractDataSourceServiceImporterMixin<ComboPooledDataSource>
            implements Activatable, ServiceImporter
    {

        @Override
        protected ComboPooledDataSource setupDataSourcePool( DataSourceConfigurationValue config )
                throws Exception
        {
            ComboPooledDataSource pool = new ComboPooledDataSource();

            Class.forName( config.driver().get() );
            pool.setDriverClass( config.driver().get() );
            pool.setJdbcUrl( config.url().get() );

            String props = config.properties().get();
            String[] properties = props.split( "," );
            Properties poolProperties = new Properties();
            for ( String property : properties ) {
                if ( property.trim().length() > 0 ) {
                    String[] keyvalue = property.trim().split( "=" );
                    poolProperties.setProperty( keyvalue[0], keyvalue[1] );
                }
            }
            pool.setProperties( poolProperties );

            if ( !config.username().get().equals( "" ) ) {
                pool.setUser( config.username().get() );
                pool.setPassword( config.password().get() );
            }
            pool.setMaxConnectionAge( 60 * 60 ); // One hour max age

            return pool;
        }

        @Override
        protected void passivateDataSourcePool( ComboPooledDataSource dataSourcePool )
                throws SQLException
        {
            DataSources.destroy( dataSourcePool );
        }

    }

}