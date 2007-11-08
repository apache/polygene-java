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

package org.qi4j.runtime.structure;

import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.structure.Assembly;
import org.qi4j.structure.AssemblyException;
import org.qi4j.structure.AssemblyRegistry;

/**
 * TODO
 */
public class AssemblyRegistryImpl
    implements AssemblyRegistry
{
    Qi4jRuntime runtime;

    Set<Assembly> assemblies = new LinkedHashSet<Assembly>();

    public AssemblyRegistryImpl( Qi4jRuntime runtime )
    {
        this.runtime = runtime;
    }

    public synchronized void addAssembly( Assembly assembly )
        throws AssemblyException
    {
        if( assemblies.contains( assembly ) )
        {
            throw new AssemblyException( "Assembly already registered" );
        }

        // Let structure configure the runtime
/*
        assembly.configure( runtime.getCompositeMapper());
        assembly.configure( new DependencyBinder()
        {
            public <T, I extends T> void registerImplementation( Class<T> dependencyType, Class<I> implementationClass )
            {
                if ( Composite.class.isAssignableFrom( implementationClass))
                {
                    // Instantiate as Composite
                    CompositeBuilder<? extends Composite> builder = runtime.newCompositeBuilderFactory().newCompositeBuilder( (Class<? extends org.qi4j.Composite>) implementationClass);
                    T instance = (T) builder.newInstance();
                    runtime.getServiceDependencyResolver().setService( dependencyType, );
                }
            }

            public <T, I extends T> void registerInstance( Class<T> dependencyType, I instance )
            {
                runtime.getServiceDependencyResolver().setService(dependencyType, instance );
            }
        });
        assembly.configure( new QueryMapper()
        {
            
        });
*/

        assemblies.add( assembly );
    }

    public synchronized void removeAssembly( Assembly assembly )
    {
        assemblies.remove( assembly );
    }

    public synchronized Iterable<Assembly> getAssemblies()
    {
        return assemblies;
    }
}
