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
package org.apache.zest.cache.ehcache;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.cache.ehcache.assembly.EhCacheAssembler;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.cache.AbstractCachePoolTest;

public class EhCacheTest
    extends AbstractCachePoolTest
{
    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
	    // END SNIPPET: assembly
        ModuleAssembly confModule = module.layer().module( "confModule" );
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( confModule );

        // START SNIPPET: assembly
        new EhCacheAssembler().
            withConfig( confModule, Visibility.layer ).
            assemble( module );
    }
    // END SNIPPET: assembly
}
