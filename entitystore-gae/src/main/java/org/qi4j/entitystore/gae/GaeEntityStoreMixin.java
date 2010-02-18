/*  Copyright 2010 Niclas Hedhman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;

/**
 * GAE implementation of SerializationStore
 */
public class GaeEntityStoreMixin
    implements Activatable, EntityStore
{
    private DatastoreService datastoreService;
    private String uuid;
    private long counter;

    public GaeEntityStoreMixin( @Service IdentityGenerator uuid )
    {
        this.uuid = uuid.generate( Identity.class );
        counter = 0L;
    }

    public void activate()
        throws Exception
    {
        datastoreService = DatastoreServiceFactory.getDatastoreService();
    }

    public void passivate()
        throws Exception
    {
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module )
    {
        return new GaeEntityStoreUnitOfWork( datastoreService, generateId() );
    }

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, Module module )
    {
        GaeEntityStoreUnitOfWork euow = new GaeEntityStoreUnitOfWork( datastoreService, generateId() );
        Query query = new Query();
        PreparedQuery q = datastoreService.prepare( query );
        QueryResultIterable<Entity> iterable = q.asQueryResultIterable();
        for( Entity entity : iterable )
        {
            EntityState entityState = new GaeEntityState( euow, entity );
            visitor.visitEntityState( entityState );
        }
        return euow;
    }

    private String generateId()
    {
        return uuid + counter++;
    }
}