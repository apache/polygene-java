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

package org.apache.zest.runtime.bootstrap.docs;

import org.apache.zest.api.structure.Application;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.runtime.bootstrap.AssemblyHelper;
import org.apache.zest.runtime.composite.FragmentClassLoader;

public class DocumentationSupport
{
    // START SNIPPET: customAssemblyHelper
    private static Energy4Java zest;

    private static Application application;

    public static void main( String[] args )
        throws Exception
    {
        // Create a Polygene Runtime
        zest = new Energy4Java();
        application = zest.newApplication( new ApplicationAssembler()
        {

            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory appFactory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = appFactory.newApplicationAssembly();
                assembly.setMetaInfo( new DalvikAssemblyHelper() );
                // END SNIPPET: customAssemblyHelper
                // START SNIPPET: customAssemblyHelper
                return assembly;
            }
        } );
        // activate the application
        application.activate();
    }

    public static class DalvikAssemblyHelper extends AssemblyHelper
    {
        @Override
        protected FragmentClassLoader instantiateFragmentClassLoader( ClassLoader parent )
        {
            return new DalvikFragmentClassLoader( parent );
        }
    }

    public static class DalvikFragmentClassLoader extends FragmentClassLoader
    {

        public DalvikFragmentClassLoader( ClassLoader parent )
        {
            super( parent );
        }
    }
    // END SNIPPET: customAssemblyHelper
}
