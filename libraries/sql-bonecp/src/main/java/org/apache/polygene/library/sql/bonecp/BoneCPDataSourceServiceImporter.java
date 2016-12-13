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
package org.apache.polygene.library.sql.bonecp;

import com.jolbox.bonecp.BoneCPDataSource;
import java.util.Properties;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.library.sql.datasource.AbstractDataSourceServiceImporterMixin;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.datasource.DataSourceServiceImporterActivation;

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

    public static class Mixin
            extends AbstractDataSourceServiceImporterMixin<BoneCPDataSource>
    {

        @Override
        protected BoneCPDataSource setupDataSourcePool( DataSourceConfiguration config )
                throws Exception
        {
            BoneCPDataSource pool = new BoneCPDataSource();

            Class.forName( config.driver().get() );
            pool.setDriverClass( config.driver().get() );
            pool.setJdbcUrl( config.url().get() );

            if ( !config.username().get().equals( "" ) ) {
                pool.setUsername( config.username().get() );
                pool.setPassword( config.password().get() );
            }
            
            if ( config.minPoolSize().get() != null ) {
                pool.setMinConnectionsPerPartition( config.minPoolSize().get() );
            }
            if ( config.maxPoolSize().get() != null ) {
                pool.setMaxConnectionsPerPartition( config.maxPoolSize().get() );
            }
            if ( config.loginTimeoutSeconds().get() != null ) {
                pool.setLoginTimeout( config.loginTimeoutSeconds().get() );
            }
            if ( config.maxConnectionAgeSeconds().get() != null ) {
                pool.setMaxConnectionAgeInSeconds( config.maxConnectionAgeSeconds().get() );
            }
            if ( config.validationQuery().get() != null ) {
                pool.setConnectionTestStatement( config.validationQuery().get() );
            }

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
