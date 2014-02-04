/*
 * Copyright 2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.cache.memcache;

import org.junit.BeforeClass;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.cache.AbstractCachePoolTest;

import static org.qi4j.test.util.Assume.assumeConnectivity;

/**
 * Memcache CachePool Test.
 */
public class MemcacheCachePoolTest
    extends AbstractCachePoolTest
{
    @BeforeClass
    public static void beforeMemcacheCacheTests()
    {
        assumeConnectivity( "localhost", 11211 );
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        ModuleAssembly confModule = module.layer().module( "confModule" );
        new EntityTestAssembler( Visibility.layer ).assemble( confModule );
        // START SNIPPET: assembly
        new MemcacheAssembler( Visibility.module ).
            withConfig( confModule, Visibility.layer ).
            assemble( module );
        // END SNIPPET: assembly
        MemcacheConfiguration memcacheConf = confModule.forMixin( MemcacheConfiguration.class ).declareDefaults();
        memcacheConf.protocol().set( "binary" );
        //memcacheConf.username().set( "foo" );
        //memcacheConf.password().set( "bar" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly
}
