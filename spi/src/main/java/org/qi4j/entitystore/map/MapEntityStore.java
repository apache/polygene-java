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
import java.io.InputStream;
import java.io.OutputStream;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityStoreException;

/**
 * JAVADOC
 */
public interface MapEntityStore
{
    boolean contains( EntityReference entityReference, Usecase usecase, MetaInfo unitofwork )
        throws EntityStoreException;

    InputStream get( EntityReference entityReference, Usecase usecase, MetaInfo unitOfWork )
        throws EntityStoreException;

    void visitMap( MapEntityStoreVisitor visitor, Usecase usecase, MetaInfo unitOfWorkMetaInfo );

    interface MapEntityStoreVisitor
    {
        void visitEntity( InputStream entityState );
    }

    void applyChanges( MapChanges changes, Usecase usecase, MetaInfo unitOfWork )
        throws IOException;

    interface MapChanges
    {
        void visitMap( MapChanger changer, Usecase usecase, MetaInfo unitOfWorkMetaInfo )
            throws IOException;
    }

    interface MapChanger
    {
        OutputStream newEntity( EntityReference ref )
            throws IOException;

        OutputStream updateEntity( EntityReference ref )
            throws IOException;

        void removeEntity( EntityReference ref )
            throws EntityNotFoundException;
    }
}
