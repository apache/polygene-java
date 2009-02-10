/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.qi4j.bootstrap;

import org.qi4j.api.structure.Application;
import org.qi4j.spi.Qi4jSPI;

/**
 * Factory for creating new Qi4j applications. Typically
 * you will implementing one or more Assemblers, which
 * is used by this factory to assemble and create applications.
 */
public interface ApplicationFactory
{
    /**
     * Create a new application with one layer and one module.
     *
     * @param assembler the assembler for the single module
     * @return the application instance
     * @throws AssemblyException if the application could not be assembled
     */
    Application newApplication( Assembler assembler )
        throws AssemblyException;

    /**
     * Create a new application with the same amount of layers
     * as the first array size, with modules according to the second array size,
     * and then use the third array for assemblers of each module. This gives you
     * a simple way to create "pancake" layered applications.
     *
     * @param assemblers the set of assemblers for the application
     * @return the application instance
     * @throws AssemblyException if the application could not be assembled
     */
    Application newApplication( Assembler[][][] assemblers )
        throws AssemblyException;

    /**
     * Create a new application by providing a whole ApplicationAssembly.
     * You are responsible for creating the assembly yourself. The advantage of this
     * method is that you can create any kind of structure for your application, as long
     * as the layers form a directed graph.
     *
     * @param applicationAssembly the assembly for the application
     * @return the application instance
     * @throws AssemblyException if the application could not be assembled
     */
    Application newApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException;

    /**
     * Create a new ApplicationAssembly that can be used for the above method.
     *
     * @return a new ApplicationAssembly
     */
    ApplicationAssembly newApplicationAssembly();

    /**
     * Get a reference to the runtime, using the SPI interface.

     * @return the runtime instance
     */
    Qi4jSPI runtime();

    
    /** Load the ApplicationModel from disk, bind and instantiate from that.
     *
     * Whenever an Application instance is created, its ApplicationModel will be saved in a binary
     * format on disk. This method can use that file to speed up the boot process for large applications, by
     * not having to examine and interpret the ApplicationModel from scratch.
     *
     * @return An Application instance.
     * @throws HibernatingApplicationInvalidException If the file on disk can not be read.
     * @throws AssemblyException If some Assembly failure occured during binding or instantiation of the application.
     */
    Application loadApplication()
        throws HibernatingApplicationInvalidException, AssemblyException;
}
