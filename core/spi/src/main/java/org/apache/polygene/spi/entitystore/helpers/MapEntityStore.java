/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.spi.entitystore.helpers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import java.util.stream.Stream;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;

/**
 * MapEntityStore.
 */
public interface MapEntityStore
{
    /**
     * @param entityReference The reference to the entity that we want to get.
     * @return Entity state Reader
     */
    Reader get( EntityReference entityReference ) throws Exception;

    /**
     * @return All entities state Readers, must be closed
     */
    Stream<Reader> entityStates() throws Exception;

    void applyChanges( MapChanges changes ) throws Exception;

    /**
     * Changes to be applied on a MapEntityStore.
     */
    interface MapChanges
    {
        /**
         * Visitable MapChanges.
         *
         * @param changer Map changer
         * @throws IOException on error
         */
        void visitMap( MapChanger changer ) throws Exception;
    }

    /**
     * MapEntityStore changes applier.
     */
    interface MapChanger
    {
        Writer newEntity( EntityReference ref, EntityDescriptor entityDescriptor )
            throws Exception;

        Writer updateEntity( MapChange mapChange ) throws Exception;

        void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
            throws Exception;
    }

    /**
     * MapEntityStore change meta info.
     *
     * Implementations backed by a shared store can make use of this for e.g. optimistic locking.
     */
    class MapChange
    {
        private final EntityReference reference;
        private final EntityDescriptor descriptor;
        private final String previousVersion;
        private final String newVersion;
        private final Instant lastModified;

        public MapChange( EntityReference reference, EntityDescriptor descriptor,
                          String previousVersion, String newVersion,
                          Instant lastModified )
        {
            this.reference = reference;
            this.descriptor = descriptor;
            this.previousVersion = previousVersion;
            this.newVersion = newVersion;
            this.lastModified = lastModified;
        }

        public EntityReference reference()
        {
            return reference;
        }

        public EntityDescriptor descriptor()
        {
            return descriptor;
        }

        /**
         * @return null if the change is an insertion
         */
        public String previousVersion()
        {
            return previousVersion;
        }

        public String newVersion()
        {
            return newVersion;
        }

        public Instant lastModified()
        {
            return lastModified;
        }
    }
}
