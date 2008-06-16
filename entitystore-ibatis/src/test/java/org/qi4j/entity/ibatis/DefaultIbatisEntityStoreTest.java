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
package org.qi4j.entity.ibatis;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerConfiguration;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerConfigurationComposite;
import org.qi4j.entity.ibatis.entity.PersonComposite;
import org.qi4j.entity.ibatis.DerbyDatabaseHandler;
import org.qi4j.structure.Visibility;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;

/**
 * TODO
 */
@Ignore
public class DefaultIbatisEntityStoreTest
    extends AbstractEntityStoreTest
{
    private DerbyDatabaseHandler derbyDatabaseHandler;
    private static final String TEST_VALUE_SQLMAP = "SqlMapConfig.xml";

    @Before public void setUp() throws Exception
    {
        derbyDatabaseHandler = new DerbyDatabaseHandler();
        super.setUp();
    }

    public final void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( PersonComposite.class );
        module.addServices( UuidIdentityGeneratorService.class );
        module.addServices( IBatisEntityStoreService.class );

        final ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addEntities( IBatisConfigurationComposite.class).visibleIn( Visibility.layer );
        config.addEntities( DBInitializerConfigurationComposite.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class );
        config.on( IBatisConfigurationComposite.class ).to().sqlMapConfigURL().set( derbyDatabaseHandler.getUrlString( TEST_VALUE_SQLMAP ));
        derbyDatabaseHandler.initDbInitializerInfo( config );
    }

    @Override @After public void tearDown() throws Exception
    {
        if (derbyDatabaseHandler!=null)
            derbyDatabaseHandler.shutdown();
        super.tearDown();
    }
}