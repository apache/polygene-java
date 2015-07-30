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
package org.apache.zest.spi.entitystore.helpers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.io.Input;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStoreException;

/**
 * MapEntityStore.
 */
public interface MapEntityStore
{

    /**
     * @param entityReference The reference to the entity that we want to get.
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
