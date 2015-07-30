/*
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
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
package org.apache.zest.test.performance.entitystore.jdbm;

import java.io.File;
import org.apache.derby.iapi.services.io.FileUtil;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.cache.ehcache.EhCacheConfiguration;
import org.apache.zest.cache.ehcache.EhCachePoolService;
import org.apache.zest.entitystore.jdbm.JdbmConfiguration;
import org.apache.zest.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.performance.entitystore.AbstractEntityStorePerformanceTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

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
        return new Assembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new JdbmEntityStoreAssembler().assemble( module );
                new OrgJsonValueSerializationAssembler().assemble( module );
                ModuleAssembly configModule = module.layer().module( "Config" );
                configModule.entities( JdbmConfiguration.class ).visibleIn( Visibility.layer );
                new EntityTestAssembler().assemble( configModule );

                module.services( EhCachePoolService.class );
                configModule.entities( EhCacheConfiguration.class ).visibleIn( Visibility.layer );
            }
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
