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

package org.qi4j.runtime.resolution;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.Composite;
import org.qi4j.CompositeBuilder;
import org.qi4j.ObjectBuilder;
import org.qi4j.Qi4j;
import org.qi4j.annotation.scope.Service;
import org.qi4j.dependency.Binding;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.dependency.InjectionKey;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.dependency.StaticDependencyResolution;
import org.qi4j.model.DependencyKey;
import org.qi4j.structure.DependencyBinder;

public class ServiceDependencyResolver
    implements DependencyResolver, DependencyBinder
{
    private Map<DependencyKey, DependencyResolution> dependencies;
    private Qi4j runtime;

    public ServiceDependencyResolver( Qi4j runtime )
    {
        this.runtime = runtime;
        dependencies = new ConcurrentHashMap<DependencyKey, DependencyResolution>();
    }

    // DependencyResolver implementation ----------------------------
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        DependencyResolution resolution = dependencies.get( key );

        if( resolution != null )
        {
            return resolution;
        }

        // Is it a concrete class?
        if( !key.getDependencyType().isInterface() )
        {
            // Try to instantiate it
            ObjectBuilder builder = runtime.newObjectBuilderFactory().newObjectBuilder( key.getDependencyType() );
            Object instance = builder.newInstance();
            resolution = new StaticDependencyResolution( instance );
            dependencies.put( key, resolution );
        }

        // Look for mapped Composite type
        Class<? extends Composite> compositeType = runtime.getCompositeRegistry().getCompositeType( key.getDependencyType() );

        if( compositeType != null )
        {
            // Instantiate it
            CompositeBuilder builder = runtime.newCompositeBuilderFactory().newCompositeBuilder( compositeType );
            Composite instance = builder.newInstance();
            resolution = new StaticDependencyResolution( instance );
            dependencies.put( key, resolution );

            return resolution;
        }

        throw new InvalidDependencyException( "No service found for key " + key );
    }


    // Public -------------------------------------------------------
    public void bind( Binding binding )
    {
        InjectionKey injectionKey = binding.getInjectionKey();
        dependencies.put( new DependencyKey( Service.class, injectionKey.getGenericType(), injectionKey.getName(), injectionKey.getDependentType() ), binding.getDependencyResolution() );
    }


    public void bind( DependencyKey key, DependencyResolver resolver, Object instance )
    {
        // TODO?
    }

    public void clear()
    {
        dependencies.clear();
    }
}
