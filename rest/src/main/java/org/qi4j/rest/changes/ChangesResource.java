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

package org.qi4j.rest.changes;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * JAVADOC
 */
public class ChangesResource
    extends ServerResource
{
    @Service EntityStore entityStore;

    public Representation put(Representation representation) throws ResourceException
    {
        String id = (String) getRequest().getAttributes().get( "id" );
        
        Representation entity = this.getRequest().getEntity();
        try
        {
            InputStream in = entity.getStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            String identity = oin.readUTF();
            Usecase usecase = (Usecase) oin.readUnshared();
            MetaInfo unitofwork = (MetaInfo) oin.readUnshared();
            Iterable<UnitOfWorkEvent> events = (Iterable<UnitOfWorkEvent>) oin.readUnshared();

            // Store state
            try
            {
                entityStore.apply( identity, events, usecase, unitofwork ).commit();
            }
            catch ( ConcurrentEntityStateModificationException e)
            {
                throw new ResourceException( Status.CLIENT_ERROR_CONFLICT);
            }
        }
        catch (Exception e)
        {
            throw new ResourceException(e);
        }

        return new EmptyRepresentation();
    }
}
