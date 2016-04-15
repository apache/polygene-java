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

import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.zest.index.rdf.assembly.RdfRdbmsSesameStoreAssembler;

public class DocumentationSupport
{

    class InMemoryAssembler
            implements Assembler
    {

        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            // START SNIPPET: memory
            new RdfMemoryStoreAssembler().assemble( module );
            // END SNIPPET: memory
        }

    }

    class NativeMemoryAssembler
            implements Assembler
    {

        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            // START SNIPPET: native
            new RdfNativeSesameStoreAssembler().assemble( module );
            // END SNIPPET: native
        }

    }

    class RDBMSMemoryAssembler
            implements Assembler
    {

        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            // START SNIPPET: rdbms
            new RdfRdbmsSesameStoreAssembler().assemble( module );
            // END SNIPPET: rdbms
        }

    }

}
