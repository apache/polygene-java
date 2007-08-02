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

import org.qi4j.api.DependencyResolver;
import org.qi4j.spi.QiHelper;
import org.qi4j.api.model.CompositeContext;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Iterator;

public class TypeLookupResolver
    implements DependencyResolver, Iterable
{
    private HashMap<Object, Object> dependencies;

    public TypeLookupResolver()
    {
        dependencies = new HashMap<Object, Object>();
    }

    public Object resolveDependency( AnnotatedElement dependentElement, CompositeContext context )
    {
        Object key = QiHelper.getDependencyKey( dependentElement );
        return dependencies.get(key);
    }

    public void put( Class key, Object value )
    {
        dependencies.put( key, value );
    }

    public void put( String key, Object value )
    {
        dependencies.put( key, value );
    }

    public Object get( Object key )
    {
        return dependencies.get( key );
    }

    public Iterator iterator()
    {
        return dependencies.entrySet().iterator();
    }

    public void clear()
    {
        dependencies.clear();
    }
}
