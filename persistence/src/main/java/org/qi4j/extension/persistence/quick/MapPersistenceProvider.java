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

import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.persistence.modifier.PersistentStorageReferenceModifier;
import org.qi4j.api.persistence.modifier.PersistentStorageTraceModifier;
import org.qi4j.runtime.persistence.spi.SerializablePersistenceSpi;
import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


/**
 * In-memory repository which stores objects in a hashmap.
 */
@ModifiedBy( { PersistentStorageTraceModifier.class, PersistentStorageReferenceModifier.class } )
public final class MapPersistenceProvider
    implements SerializablePersistenceSpi
{
    Map<String, Map<Class, MarshalledObject>> repository = new HashMap<String, Map<Class, MarshalledObject>>();

    public void removeInstance( String aId )
    {
        repository.remove( aId );
    }

    public void close() throws IOException
    {
        repository = null;
    }

    public Map<Class, MarshalledObject> getInstance( String aId )
    {
        return repository.get( aId );
    }

    public void putInstance( String aId, Map<Class, MarshalledObject> aPersistentMixins )
    {
        repository.put( aId, aPersistentMixins );
    }
}
