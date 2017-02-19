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
package org.apache.polygene.entitystore.cassandra;

import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;


public class DocSupport
    implements Assembler
{
// START-SNIPPET: assembly
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
// END-SNIPPET: assembly
        module.services( ClusterBuilder.class ).withMixins( MyClusterBuilder.class );
// START-SNIPPET: assembly
    }
// END-SNIPPET: assembly

// START-SNIPPET: builder
    public class MyClusterBuilder extends ClusterBuilder.DefaultBuilder
        implements ClusterBuilder
    {
        @Structure
        private Application application;

        @Override
        protected String hostnames()
        {
            switch( application.mode() )
            {
            case development:
                return "localhost:9042";
            case staging:
                return "cassandra.staging:9042";
            case production:
                return "cassandra1.prod:9042,cassandra2.prod:9042,cassandra3.prod:9042";
            case test:
            default:
                return "cassandra.test:9042";
            }
        }
    }
// END-SNIPPET: builder
}
