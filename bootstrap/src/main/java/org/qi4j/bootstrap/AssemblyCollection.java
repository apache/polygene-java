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
 * Assembly that delegates to a collection of Assemblies.
 * <p/>
 * Makes it easy to collect and compose assemblies into bigger assemblies.
 */
public final class AssemblyCollection
    implements Assembly
{
    Collection<Assembly> assemblies;

    public AssemblyCollection( Assembly... assemblies )
    {
        this.assemblies = Arrays.asList( assemblies );
    }

    public AssemblyCollection( Class<? extends Assembly>... assemblyClasses )
        throws AssemblyException
    {
        assemblies = new ArrayList<Assembly>();
        for( Class<? extends Assembly> assemblyClass : assemblyClasses )
        {
            try
            {
                Assembly assembly = assemblyClass.newInstance();
                assemblies.add( assembly );
            }
            catch( Exception e )
            {
                throw new AssemblyException( "Could not instantiate assembly with class " + assemblyClass.getName(), e );
            }
        }
    }

    public AssemblyCollection( Collection<Assembly> assemblies )
    {
        this.assemblies = assemblies;
    }

    public void configure( ModuleAssembly module )
        throws AssemblyException
    {
        for( Assembly assembly : assemblies )
        {
            assembly.configure( module );
        }
    }
}
