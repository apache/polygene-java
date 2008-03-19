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
package org.qi4j.extension.persistence.quick;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.spi.serialization.SerializedObject;


/**
 * In-memory repository which stores objects in a hashmap.
 */
public final class MapPersistenceProvider
    // implements SerializationStore
{
    private Map<String, Map<Class, SerializedObject>> repository;

    public MapPersistenceProvider()
    {
        repository = new HashMap<String, Map<Class, SerializedObject>>();
    }

    public void removeInstance( String aId )
    {
        repository.remove( aId );
    }

    public void close() throws IOException
    {
        repository = null;
    }

    public Map<Class, SerializedObject> getInstance( String aId )
    {
        return repository.get( aId );
    }

    public void putInstance( String aId, Map<Class, SerializedObject> aPersistentMixins )
    {
        repository.put( aId, aPersistentMixins );
    }
}
