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

/**
 * Factory for creating new Qi4j application assemblies. Typically
 * you will implement one or more Assemblers, wrap them in an ApplicationAssembler,
 * which then uses this factory to assemble and create applications.
 */
public interface ApplicationAssemblyFactory
{
    /**
     * Create a new application with one layer and one module.
     *
     * @param assembler the assembler for the single module
     *
     * @return the application instance
     *
     * @throws AssemblyException if the application could not be assembled
     */
    ApplicationAssembly newApplicationAssembly( Assembler assembler )
        throws AssemblyException;

    /**
     * Create a new application with the same amount of layers
     * as the first array size, with modules according to the second array size,
     * and then use the third array for assemblers of each module. This gives you
     * a simple way to create "pancake" layered applications.
     *
     * @param assemblers the set of assemblers for the application
     *
     * @return the application instance
     *
     * @throws AssemblyException if the application could not be assembled
     */
    ApplicationAssembly newApplicationAssembly( Assembler[][][] assemblers )
        throws AssemblyException;

    /**
     * Create a new ApplicationAssembly that can be used for the above method.
     *
     * @return a new ApplicationAssembly
     */
    ApplicationAssembly newApplicationAssembly();
}
