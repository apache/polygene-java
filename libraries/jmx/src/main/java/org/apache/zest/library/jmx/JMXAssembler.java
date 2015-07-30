/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.zest.library.jmx;

import javax.management.MBeanServer;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;

/**
 * Register JMX helpers in a module.
 */
public class JMXAssembler
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.importedServices( MBeanServer.class ).importedBy( MBeanServerImporter.class );
        module.services( ApplicationManagerService.class ).instantiateOnStartup();
        module.services( ConfigurationManagerService.class ).instantiateOnStartup();

    }
}
