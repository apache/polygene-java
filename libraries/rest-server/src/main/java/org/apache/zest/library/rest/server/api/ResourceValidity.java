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

package org.apache.zest.library.rest.server.api;

import java.util.Date;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.spi.ZestSPI;
import org.apache.zest.spi.entity.EntityState;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class ResourceValidity
{
    EntityComposite entity;
    private final ZestSPI spi;
    private Request request;

    public ResourceValidity( EntityComposite entity, ZestSPI spi, Request request )
    {
        this.entity = entity;
        this.spi = spi;
        this.request = request;
    }

    public void updateEntity( UnitOfWork current )
    {
        try
        {
            entity = current.get( entity );
        }
        catch( NoSuchEntityException e )
        {
            // Entity was deleted
            entity = null;
        }
    }

    public void updateResponse( Response response )
    {
        if( entity != null )
        {
            EntityState state = spi.entityStateOf( entity );
            Date lastModified = new Date( state.lastModified() );
            Tag tag = new Tag( state.identity().identity() + "/" + state.version() );
            response.getEntity().setModificationDate( lastModified );
            response.getEntity().setTag( tag );
        }
    }

    public void checkRequest()
        throws ResourceException
    {
        // Check command rules
        Date modificationDate = request.getConditions().getUnmodifiedSince();
        if( modificationDate != null )
        {
            EntityState state = spi.entityStateOf( entity );
            Date lastModified = new Date( ( state.lastModified() / 1000 ) * 1000 ); // Cut off milliseconds
            if( lastModified.after( modificationDate ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
            }
        }

        // Check query rules
        modificationDate = request.getConditions().getModifiedSince();
        if( modificationDate != null )
        {
            EntityState state = spi.entityStateOf( entity );
            Date lastModified = new Date( ( state.lastModified() / 1000 ) * 1000 ); // Cut off milliseconds
            if( !lastModified.after( modificationDate ) )
            {
                throw new ResourceException( Status.REDIRECTION_NOT_MODIFIED );
            }
        }
    }
}
