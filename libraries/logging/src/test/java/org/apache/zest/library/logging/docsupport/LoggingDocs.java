/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.library.logging.docsupport;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.logging.debug.Debug;
import org.apache.zest.library.logging.trace.Trace;
import org.apache.zest.library.logging.trace.TraceAllConcern;

public class LoggingDocs
{

// START SNIPPET: logging1
    @Optional @This Debug debug;
// END SNIPPET: logging1

    public LoggingDocs()
    {
// START SNIPPET: logging2
        if( debug != null )
        {
            debug.debug( Debug.NORMAL, "Debugging is made easier." );
        }
// END SNIPPET: logging2
     }

// START SNIPPET: logging3
    public interface ImportantRepository
    {
        @Trace
        void addImportantStuff( ImportantStuff stuff );

        @Trace
        void removeImportantStuff( ImportantStuff stuff );

        ImportantStuff findImportantStuff( String searchKey );
    }
// END SNIPPET: logging3

// START SNIPPET: logging4
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.addServices(ImportantRepository.class)
                .withConcerns( TraceAllConcern.class )
                .withMixins( Debug.class );
    }

// END SNIPPET: logging4

    class ImportantStuff {}
}
