/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entitystore.map;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 * JAVADOC
 */
public interface MapEntityStore
{
    // JSON keys for values in the stored data

    enum JSONKeys
    {
        identity,           // Identity of the entity
        application_version,// Version of the application which last updated the entity
        type,               // Type of the entity
        version,            // Version of the entity
        modified,           // When entity was last modified according to System.currentTimeMillis()
        properties,         // Map of properties
        associations,       // Map of associations
        manyassociations    // Map of manyassociations
    }

    Reader get( EntityReference entityReference )
        throws EntityStoreException;

    void visitMap( MapEntityStoreVisitor visitor );

    interface MapEntityStoreVisitor
    {
        void visitEntity( Reader entityState );
    }

    void applyChanges( MapChanges changes )
        throws IOException;

    interface MapChanges
    {
        void visitMap( MapChanger changer )
            throws IOException;
    }

    interface MapChanger
    {
        Writer newEntity( EntityReference ref, EntityType entityType )
            throws IOException;

        Writer updateEntity( EntityReference ref, EntityType entityType )
            throws IOException;

        void removeEntity( EntityReference ref, EntityType entityType )
            throws EntityNotFoundException;
    }
}
