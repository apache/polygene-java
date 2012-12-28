/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.api.service;

import java.util.List;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.qualifier.ServiceTags;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;

public class DocumentationSupport
    implements Assembler
{
    // START SNIPPET: tag
    // START SNIPPET: instantiateOnStartup
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ServiceDeclaration service = module.addServices( MyDemoService.class );
        // END SNIPPET: tag
        service.instantiateOnStartup();
        // END SNIPPET: instantiateOnStartup
        // START SNIPPET: tag
        service.taggedWith( "Important", "Drain" );
        // END SNIPPET: tag
    }

    private static class MyDemoService
    {
    }

    private static class MyOtherDemoService
    {
        // START SNIPPET: UseTag
        @Service
        private List<ServiceReference<MyDemoService>> services;

        public MyDemoService locateImportantService()
        {
            for( ServiceReference<MyDemoService> ref : services )
            {
                ServiceTags serviceTags = ref.metaInfo( ServiceTags.class );
                if( serviceTags.hasTag( "Important" ) )
                {
                    return ref.get();
                }
            }
            return null;
        }
        // END SNIPPET: UseTag
    }
}
