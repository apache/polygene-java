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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Assembler that delegates to a collection of Assemblers.
 * <p/>
 * Makes it easy to collect and compose assemblers into bigger assemblers.
 */
public final class AssemblerCollection
    implements Assembler
{
    Collection<Assembler> assemblers;

    public AssemblerCollection( Assembler... assemblers )
    {
        this.assemblers = Arrays.asList( assemblers );
    }

    @SafeVarargs
    public AssemblerCollection( Class<? extends Assembler>... assemblyClasses )
        throws AssemblyException
    {
        assemblers = new ArrayList<>();
        for( Class<? extends Assembler> assemblyClass : assemblyClasses )
        {
            try
            {
                Assembler assembler = assemblyClass.newInstance();
                assemblers.add( assembler );
            }
            catch( Exception e )
            {
                throw new AssemblyException( "Could not instantiate assembly with class " + assemblyClass.getName(), e );
            }
        }
    }

    public AssemblerCollection( Collection<Assembler> assemblers )
    {
        this.assemblers = assemblers;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        for( Assembler assembler : assemblers )
        {
            assembler.assemble( module );
        }
    }
}
