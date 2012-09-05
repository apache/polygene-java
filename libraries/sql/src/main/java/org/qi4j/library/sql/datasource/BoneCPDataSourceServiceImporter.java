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

import com.jolbox.bonecp.BoneCPDataSource;
import java.util.Properties;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.mixin.Mixins;

/**
 * DataSource service implemented as a ServiceImporter.
 *
 * Import visible DataSources as Services. The default Mixin use the BoneCP
 * pooling system optionaly wrapped with CircuitBreaker using a proxy.
 */
@Mixins( BoneCPDataSourceServiceImporter.Mixin.class )
@Activators( DataSourceServiceImporterActivation.Activator.class )
public class BoneCPDataSourceServiceImporter
{

    class Mixin
            extends AbstractDataSourceServiceImporterMixin<BoneCPDataSource>
    {

        @Override
        protected BoneCPDataSource setupDataSourcePool( DataSourceConfigurationValue config )
                throws Exception
        {
            BoneCPDataSource pool = new BoneCPDataSource();

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
                pool.setUsername( config.username().get() );
                pool.setPassword( config.password().get() );
            }
            pool.setMaxConnectionAgeInSeconds( 60 * 60 ); // One hour max age

            return pool;
        }

        @Override
        protected void passivateDataSourcePool( BoneCPDataSource dataSourcePool )
                throws Exception
        {
            dataSourcePool.close();
        }

    }

}
