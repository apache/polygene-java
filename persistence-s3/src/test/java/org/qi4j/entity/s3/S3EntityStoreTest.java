/*  Copyright 2008 Rickard …berg.
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
package org.qi4j.entity.s3;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.property.PropertyMapper;
import org.qi4j.structure.Visibility;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * Amazon S3 EntityStore test
 */
public class S3EntityStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( S3EntityStoreComposite.class ).instantiateOnStartup();

        ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addComposites( S3ConfigurationComposite.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreComposite.class );
    }

    @Override @Before public void setUp() throws Exception
    {
        super.setUp();

        UnitOfWorkFactory uowf = application.getLayerByName( "Layer 1" ).getModuleByName( "config" ).getStructureContext().getUnitOfWorkFactory();
        UnitOfWork uow = uowf.newUnitOfWork();
        S3ConfigurationComposite config = uow.newEntityBuilder( "s3configuration", S3ConfigurationComposite.class ).newInstance();

        PropertyMapper.map( getClass().getResourceAsStream( "s3configuration.properties" ), config );

        uow.complete();
    }

    @Test
    public void dummyTest()
    {
        // All tests are disabled since by default the S3 store doesn't work due to missing account keys!
    }
}