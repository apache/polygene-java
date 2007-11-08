/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.runtime.resolution;

import java.lang.reflect.Type;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.CompositeRegistry;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.dependency.StaticDependencyResolution;
import org.qi4j.model.DependencyKey;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.structure.CompositeMapper;

public class StructureDependencyResolver
    implements DependencyResolver
{
    private Qi4jRuntime runtime;

    public StructureDependencyResolver( Qi4jRuntime runtime )
    {
        this.runtime = runtime;
    }

    /**
     * Find a dependency resolution given a dependency key and a thisAs model. If no resolution
     * can be found, return null. If the dependency is optional the dependency will
     * then be explicitly set to null.
     *
     * @param key
     * @return
     */
    public DependencyResolution resolveDependency( DependencyKey key ) throws InvalidDependencyException
    {
        Type type = key.getGenericType();

        // API
        if( type.equals( CompositeBuilderFactory.class ) )
        {
            return new StaticDependencyResolution( runtime.newCompositeBuilderFactory() );
        }
        if( type.equals( ObjectBuilderFactory.class ) )
        {
            return new StaticDependencyResolution( runtime.newObjectBuilderFactory() );
        }
        if( type.equals( CompositeMapper.class ) )
        {
            return new StaticDependencyResolution( runtime.getCompositeRegistry() );
        }
        if( type.equals( CompositeRegistry.class ) )
        {
            return new StaticDependencyResolution( runtime.getCompositeRegistry() );
        }

        // SPI
        if( type.equals( CompositeRegistry.class ) )
        {
            return new StaticDependencyResolution( runtime.getDependencyResolverRegistry() );
        }

        return null;
    }
}
