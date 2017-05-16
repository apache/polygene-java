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
 */
package org.apache.polygene.test.performance.entitystore.jdbm;

import java.io.File;
import org.apache.derby.iapi.services.io.FileUtil;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.cache.ehcache.EhCacheConfiguration;
import org.apache.polygene.cache.ehcache.EhCachePoolService;
import org.apache.polygene.entitystore.jdbm.JdbmConfiguration;
import org.apache.polygene.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.performance.entitystore.AbstractEntityStorePerformanceTest;

/**
 * Performance test for JdbmEntityStoreComposite
 */
public class JdbmEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{
    public JdbmEntityStorePerformanceTest()
    {
        super( "JdbmEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return module ->
        {
            new JdbmEntityStoreAssembler().assemble( module );
            ModuleAssembly configModule = module.layer().module( "Config" );
            configModule.entities( JdbmConfiguration.class ).visibleIn( Visibility.layer );
            new EntityTestAssembler().assemble( configModule );

            module.services( EhCachePoolService.class );
            configModule.entities( EhCacheConfiguration.class ).visibleIn( Visibility.layer );
        };
    }

    @Override
    public void cleanUp()
        throws Exception
    {
        super.cleanUp();
        FileUtil.removeDirectory( new File( "build/tmp/jdbm" ) );
    }
}
