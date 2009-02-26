/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.bootstrap.spi;

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.HibernatingApplicationInvalidException;
import org.qi4j.api.structure.Application;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationSPI;

/**
 * This interface is implemented by Qi4j runtimes
 */
public interface ApplicationFactory
{
    ApplicationSPI newApplication( ApplicationAssembly assembly)
        throws AssemblyException;

    /** Load the ApplicationModel from disk, bind and instantiate from that.
     *
     * Whenever an Application instance is created, its ApplicationModel will be saved in a binary
     * format on disk. This method can use that file to speed up the boot process for large applications, by
     * not having to examine and interpret the ApplicationModel from scratch.
     *
     * @return An Application instance.
     * @throws org.qi4j.bootstrap.HibernatingApplicationInvalidException If the file on disk can not be read.
     * @throws AssemblyException If some Assembly failure occured during binding or instantiation of the application.
     */
    ApplicationSPI loadApplication()
        throws HibernatingApplicationInvalidException, AssemblyException;
}
