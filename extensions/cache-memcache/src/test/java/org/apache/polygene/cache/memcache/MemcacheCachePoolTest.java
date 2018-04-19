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
package org.apache.polygene.cache.memcache;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.cache.memcache.assembly.MemcacheAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.cache.AbstractCachePoolTest;

/**
 * Memcache CachePool Test.
 */
@Docker( image = "memcached",
         ports = @Port( exposed = 11211, inner = 11211 ),
         newForEachCase = false )
public class MemcacheCachePoolTest
    extends AbstractCachePoolTest
{
    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        try
        {
            Thread.sleep(10000);
        }
        catch( InterruptedException e )
        {
            e.printStackTrace();
        }
        ModuleAssembly confModule = module.layer().module( "confModule" );
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( confModule );
        // START SNIPPET: assembly
        new MemcacheAssembler().
            visibleIn( Visibility.module ).
            withConfig( confModule, Visibility.layer ).
            assemble( module );
        // END SNIPPET: assembly
        MemcacheConfiguration memcacheConf = confModule.forMixin( MemcacheConfiguration.class ).declareDefaults();
        String dockerHost = "localhost";
        int dockerPort = 11211;

        memcacheConf.addresses().set( dockerHost + ':' + dockerPort );
        memcacheConf.protocol().set( "binary" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly
}
