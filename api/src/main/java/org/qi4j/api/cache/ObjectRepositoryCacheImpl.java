/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.cache;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.persistence.binding.PersistenceBinding;

/**
 * Implementation of a proxy cache.
 */
public final class ObjectRepositoryCacheImpl
    implements ObjectRepositoryCache
{
    private static Map<String, Object> cache = new HashMap<String, Object>();

    public <T extends PersistenceBinding> T getObject( String anIdentity )
    {
        return (T) cache.get( anIdentity );
    }

    public <T extends PersistenceBinding> void addObject( String anIdentity, T anObject )
    {
        cache.put( anIdentity, anObject );
    }

    public void removeObject( String anIdentity )
    {
        cache.remove( anIdentity );
    }

    public void removeAll()
    {
        cache.clear();
    }
}
