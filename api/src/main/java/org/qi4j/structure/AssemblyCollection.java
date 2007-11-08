/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.qi4j.ObjectBuilder;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.annotation.scope.Structure;

/**
 * Assembly that delegates to a collection of Assemblies.
 * <p/>
 * Makes it easy to collect and compose assemblies into bigger assemblies.
 */
public class AssemblyCollection
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
                throw new AssemblyException( "Could not instantiate structure with class " + assemblyClass.getName(), e );
            }
        }
    }

    public AssemblyCollection( Collection<Assembly> assemblies )
    {
        this.assemblies = assemblies;
    }

    public void init( @Structure ObjectBuilderFactory obf )
    {
        for( Assembly assembly : assemblies )
        {
            ObjectBuilder builder = obf.newObjectBuilder( assembly.getClass() );
            builder.inject( assembly );
        }
    }

    public void configure( CompositeMapper mapper )
    {
        for( Assembly assembly : assemblies )
        {
            assembly.configure( mapper );
        }
    }

    public void configure( DependencyBinder mapper )
    {
        for( Assembly assembly : assemblies )
        {
            assembly.configure( mapper );
        }
    }

    public void configure( QueryMapper mapper )
    {
        for( Assembly assembly : assemblies )
        {
            assembly.configure( mapper );
        }
    }
}
