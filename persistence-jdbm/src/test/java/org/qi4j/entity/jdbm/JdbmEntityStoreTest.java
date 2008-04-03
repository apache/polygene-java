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
package org.qi4j.entity.jdbm;

import java.io.File;
import org.junit.After;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.structure.Visibility;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * TODO
 */
public class JdbmEntityStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( JdbmEntityStoreComposite.class );

        ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addComposites( JdbmConfigurationComposite.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreComposite.class );
    }

    @Override @After public void tearDown() throws Exception
    {
        super.tearDown();

        boolean deleted = new File( "qi4j.data.db" ).delete();
        deleted = deleted | new File( "qi4j.data.lg" ).delete();
        if( !deleted )
        {
            throw new Exception( "Could not delete test data" );
        }
    }
}