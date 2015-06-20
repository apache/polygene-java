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
package org.qi4j.library.osgi;

import org.osgi.framework.BundleContext;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class DocumentationSupport
{

    public static class Export
        implements Assembler
    {

        // START SNIPPET: export
        interface MyQi4jService
            extends OSGiEnabledService
        {
            // ...
        }
        // END SNIPPET: export

        @Override
        // START SNIPPET: export
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            BundleContext bundleContext = // ...
                          // END SNIPPET: export
                          null;
            // START SNIPPET: export
            module.services( OSGiServiceExporter.class ).
                setMetaInfo( bundleContext );
            module.services( MyQi4jService.class );
        }
        // END SNIPPET: export

    }

    interface MyOSGiService
    {
    }

    interface MyOtherOSGiService
    {
    }

    static class MyFallbackStrategy
    {
    }

    public static class Import
        implements Assembler
    {

        @Override
        // START SNIPPET: import
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            BundleContext bundleContext = // END SNIPPET: import
                          null;
            // START SNIPPET: import
            module.services( OSGiServiceImporter.class ).
                setMetaInfo( new OSGiImportInfo( bundleContext,
                                                 MyOSGiService.class,
                                                 MyOtherOSGiService.class ) ).
                setMetaInfo( new MyFallbackStrategy() );
        }
        // END SNIPPET: import

    }

}
