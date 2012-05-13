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

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( DBCPDataSourceServiceImporter.Mixin.class )
public interface DBCPDataSourceServiceImporter
        extends ServiceImporter, Activatable, ServiceComposite
{

    class Mixin
            implements Activatable, ServiceImporter
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( DBCPDataSourceServiceImporter.class );

        @Structure
        private Module module;

        private Map<String, DataSourceConfiguration> configs = new HashMap<String, DataSourceConfiguration>();

        public void activate()
                throws Exception
        {
        }

        public void passivate()
                throws Exception
        {
            // WARN Closes all configuration UoWs
            for ( DataSourceConfiguration dataSourceConfiguration : configs.values() ) {
                module.getUnitOfWork( dataSourceConfiguration ).discard();
            }
            configs.clear();
        }

        public Object importService( ImportedServiceDescriptor serviceDescriptor )
                throws ServiceImporterException
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isActive( Object instance )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean isAvailable( Object instance )
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

    }

}
