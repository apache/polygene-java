/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.bootstrap;

/**
 * ModuleAssemblies are configured by Assemblers. This
 * is the interface you would implement in order to provide
 * all configuration and additional metainfo that is needed
 * to instantiate a Qi4j application.
 */
public interface Assembler
{
    /**
     * Assemblers receive a callback to the ModuleAssembly
     * they are supposed to configure. They can use this
     * to register objects, composites, services etc. and
     * the additional metadata that may exist for these
     * artifacts.
     * <p/>
     * An Assembler may create new Modules by calling
     * {@link org.qi4j.bootstrap.ModuleAssembly#layerAssembly()} and
     * then {@link LayerAssembly#newModuleAssembly(String)}.
     * This allows an Assembler to bootstrap an entire Layer with
     * more Modules.
     *
     * @param module the Module to assemble
     *
     * @throws AssemblyException thrown if the assembler tries to do something illegal
     */
    void assemble( ModuleAssembly module )
        throws AssemblyException;
}
