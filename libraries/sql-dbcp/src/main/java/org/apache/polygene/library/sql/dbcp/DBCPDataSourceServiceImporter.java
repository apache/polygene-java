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
package org.apache.polygene.library.sql.dbcp;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.library.sql.datasource.AbstractDataSourceServiceImporterMixin;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.datasource.DataSourceServiceImporterActivation;

@Mixins( DBCPDataSourceServiceImporter.Mixin.class )
@Activators( DataSourceServiceImporterActivation.Activator.class )
public interface DBCPDataSourceServiceImporter
        extends ServiceImporter<DataSource>, DataSourceServiceImporterActivation, ServiceComposite
{

    class Mixin extends AbstractDataSourceServiceImporterMixin<BasicDataSource>
    {

        @Override
        protected BasicDataSource setupDataSourcePool( DataSourceConfiguration config )
                throws Exception
        {
            BasicDataSource pool = new BasicDataSource();

            Class.forName( config.driver().get() );
            pool.setDriverClassName( config.driver().get() );
            pool.setUrl( config.url().get() );

            if ( !config.username().get().equals( "" ) ) {
                pool.setUsername( config.username().get() );
                pool.setPassword( config.password().get() );
            }

            if ( config.minPoolSize().get() != null ) {
                pool.setMinIdle( config.minPoolSize().get() );
            }
            if ( config.maxPoolSize().get() != null ) {
                pool.setMaxTotal( config.maxPoolSize().get() );
            }
            if ( config.loginTimeoutSeconds().get() != null ) {
                pool.setLoginTimeout( config.loginTimeoutSeconds().get() );
            }
            if ( config.maxConnectionAgeSeconds().get() != null ) {
                pool.setMinEvictableIdleTimeMillis( config.maxConnectionAgeSeconds().get() * 1000 );
            }
            if ( config.validationQuery().get() != null ) {
                pool.setValidationQuery( config.validationQuery().get() );
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
