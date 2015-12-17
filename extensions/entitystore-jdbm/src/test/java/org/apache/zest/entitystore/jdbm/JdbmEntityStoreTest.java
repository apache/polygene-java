/*  Copyright 2008 Rickard Öberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.entitystore.jdbm;

import org.junit.Before;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.apache.zest.library.fileconfig.FileConfiguration;
import org.apache.zest.library.fileconfig.FileConfigurationDataWiper;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.entity.AbstractEntityStoreTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class JdbmEntityStoreTest
    extends AbstractEntityStoreTest
{

    @Before
    public void testDataCleanup()
    {
        FileConfiguration fileConfig = serviceFinder.findService( FileConfiguration.class ).get();
        FileConfigurationDataWiper.registerApplicationPassivationDataWiper( fileConfig, application );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        config.services( FileConfigurationService.class ).visibleIn( Visibility.layer ).instantiateOnStartup();
        new EntityTestAssembler().assemble( config );

        new OrgJsonValueSerializationAssembler().assemble( module );
        new JdbmEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
    }

}
