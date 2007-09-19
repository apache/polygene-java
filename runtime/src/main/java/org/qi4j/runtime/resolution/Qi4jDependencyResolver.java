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
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.model.DependencyKey;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;
import org.qi4j.spi.dependency.DependencyInjectionContext;
import org.qi4j.spi.dependency.DependencyResolution;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.InvalidDependencyException;

public class Qi4jDependencyResolver
    implements DependencyResolver
{
    private CompositeBuilderFactoryImpl compositeBuilderFactory;

    public Qi4jDependencyResolver( CompositeBuilderFactoryImpl compositeBuilderFactory )
    {
        this.compositeBuilderFactory = compositeBuilderFactory;
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
        if( type.equals( CompositeBuilderFactory.class ) )
        {
            return new CompositeBuilderDependencyResolution();
        }
        return null;
    }

    private class CompositeBuilderDependencyResolution
        implements DependencyResolution
    {
        /**
         * Get the resolved dependency, given the actual thisAs and the thisAs context.
         * <p/>
         * The resulting iterable may return an iterator that gives different results on each
         * invocation. This allows for dynamic updates of the resolved object result during the
         * lifetime of the injected fragment.
         *
         * @param context
         * @return iterable of result. May not be null.
         */
        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            return compositeBuilderFactory;
        }
    }
}
