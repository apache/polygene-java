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

package org.qi4j.bootstrap;

/**
 * Helper base class for application assemblers that
 * want to either create applications using only one layer/module,
 * or that wants to create pancake-layered applications.
 */
public class ApplicationAssemblerAdapter
    implements ApplicationAssembler
{
    private final Assembler[][][] assemblers;

    protected ApplicationAssemblerAdapter( Assembler assembler )
    {
        this.assemblers = new Assembler[][][]{ { { assembler } } };
    }

    protected ApplicationAssemblerAdapter( Assembler[][][] assemblers )
    {
        this.assemblers = assemblers;
    }

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        return applicationFactory.newApplicationAssembly( assemblers );
    }
}
