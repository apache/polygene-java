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
package org.qi4j.spi.entitystore.helpers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.io.Input;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 * MapEntityStore.
 */
public interface MapEntityStore
{

    /**
     * JSON keys for values in the stored data.
     */
    enum JSONKeys
    {

        /**
         * Identity of the entity.
         */
        identity,
        /**
         * Version of the application which last updated the entity.
         */
        application_version,
        /**
         * Type of the entity.
         */
        type,
        /**
         * Version of the entity.
         */
        version,
        /**
         * When entity was last modified according to System.currentTimeMillis().
         */
        modified,
        /**
         * Map of properties.
         */
        properties,
        /**
         * Map of associations.
         */
        associations,
        /**
         * Map of manyassociations.
         */
        manyassociations
    }

    /**
     * @return Entity state Reader
     */
    Reader get( EntityReference entityReference )
        throws EntityStoreException;

    /**
     * @return All entities state Readers
     */
    Input<Reader, IOException> entityStates();

    void applyChanges( MapChanges changes )
        throws IOException;

    /**
     * Changes to be applied on a MapEntityStore.
     */
    interface MapChanges
    {

        /**
         * Visitable MapChanges.
         */
        void visitMap( MapChanger changer )
            throws IOException;

    }

    /**
     * MapEntityStore changes applier.
     */
    interface MapChanger
    {

        Writer newEntity( EntityReference ref, EntityDescriptor entityDescriptor )
            throws IOException;

        Writer updateEntity( EntityReference ref, EntityDescriptor entityDescriptor )
            throws IOException;

        void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
            throws EntityNotFoundException;

    }

}
