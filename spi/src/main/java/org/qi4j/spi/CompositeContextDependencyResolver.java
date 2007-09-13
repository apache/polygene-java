/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi;

import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.DependencyKey;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.StaticDependencyResolution;
import org.qi4j.api.model.CompositeContext;

public class CompositeContextDependencyResolver
    implements DependencyResolver
{
    CompositeContext context;

    public CompositeContextDependencyResolver( CompositeContext context )
    {
        this.context = context;
    }

    public DependencyResolution resolveDependency( DependencyKey key )
    {
        Class type = key.getDependencyType();
        if( type.equals( CompositeBuilderFactory.class ) )
        {
            return new StaticDependencyResolution(type.cast(context.getCompositeBuilderFactory()));
        }
        else if( type.equals( CompositeModelFactory.class ) )
        {
            return new StaticDependencyResolution(type.cast(context.getCompositeModelFactory()));
        }
        else
        {
            return null;
        }
    }
}
