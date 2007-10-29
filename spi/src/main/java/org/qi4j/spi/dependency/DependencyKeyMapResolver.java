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
package org.qi4j.spi.dependency;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.annotation.scope.Service;
import org.qi4j.model.DependencyKey;

public class DependencyKeyMapResolver
    implements DependencyResolver, Iterable
{
    private Map<DependencyKey, DependencyResolution> dependencies;

    public DependencyKeyMapResolver()
    {
        dependencies = new ConcurrentHashMap<DependencyKey, DependencyResolution>();
    }

    // DependencyResolver implementation ----------------------------
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        return dependencies.get( key );
    }


    // Public -------------------------------------------------------
    public void set( DependencyKey key, Object value )
    {
        dependencies.put( key, new StaticDependencyResolution( value ) );
    }

    public void setService( Class key, Object value )
    {
        dependencies.put( new DependencyKey( Service.class, key, null, null ), new StaticDependencyResolution( value ) );
    }

    public void setService( final String key, Class type, Object value )
    {
        dependencies.put( new DependencyKey( Service.class, type, key, null ), new StaticDependencyResolution( value ) );
    }

    public Object getService( Class key )
    {
        return dependencies.get( new DependencyKey( Service.class, key, null, null ) ).getDependencyInjection( null );
    }

    public Object getService( String key )
    {
        return dependencies.get( new DependencyKey( Service.class, null, key, null ) ).getDependencyInjection( null );
    }

    public Iterator<Map.Entry<DependencyKey, DependencyResolution>> iterator()
    {
        return dependencies.entrySet().iterator();
    }

    public void clear()
    {
        dependencies.clear();
    }
}
