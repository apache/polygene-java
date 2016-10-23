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

package org.apache.zest.index.rdf;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.layered.ModuleAssembler;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.index.rdf.query.RdfQueryService;
import org.apache.zest.spi.query.EntityFinder;
import org.apache.zest.test.indexing.layered.AbstractMultiLayeredIndexingTest;
import org.junit.Ignore;

@Ignore("Disabled until the new Query sturcture is in place, properly supporting multilayered applications.")
public class MultiLayeredTest extends AbstractMultiLayeredIndexingTest
{
    public MultiLayeredTest()
    {
        super( IndexingModuleAssembler.class );
    }

    static class IndexingModuleAssembler
        implements ModuleAssembler
    {
        @Override
        public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
            throws AssemblyException
        {
            new RdfMemoryStoreAssembler( Visibility.application, Visibility.module ).assemble( module );
            return module;
        }
    }
}
