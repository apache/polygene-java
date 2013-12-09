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
package org.qi4j.entitystore.jdbm;

import org.junit.Before;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationDataWiper;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class JdbmEntityStoreTest
    extends AbstractEntityStoreTest
{

    @Before
    public void testDataCleanup()
    {
        FileConfiguration fileConfig = module.findService( FileConfiguration.class ).get();
        FileConfigurationDataWiper.registerApplicationPassivationDataWiper( fileConfig, application );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        config.services( FileConfigurationService.class ).visibleIn( Visibility.layer ).instantiateOnStartup();
        new EntityTestAssembler( Visibility.module ).assemble( config );

        new OrgJsonValueSerializationAssembler().assemble( module );
        new JdbmEntityStoreAssembler( Visibility.module ).withConfig( config, Visibility.layer ).assemble( module );
    }

}
