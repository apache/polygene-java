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
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.structure.ModuleSPI;

/**
 * GAE implementation of SerializationStore
 */
public class GaeEntityStoreMixin
        implements Activatable, EntityStore
{
   private DatastoreService datastoreService;
   private String uuid;
   private long counter;

   public GaeEntityStoreMixin(@Service IdentityGenerator uuid)
   {
      System.out.println("Initializing GAE EntityStore.");
      this.uuid = uuid.generate(Identity.class) + ":";
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

   public EntityStoreUnitOfWork newUnitOfWork(Usecase usecase, ModuleSPI module)
   {
      return new GaeEntityStoreUnitOfWork(datastoreService, generateId(), module);
   }

   public Input<EntityState, EntityStoreException> entityStates(final ModuleSPI module)
   {
      return new Input<EntityState, EntityStoreException>()
      {
         @Override
         public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super EntityState, ReceiverThrowableType> output) throws EntityStoreException, ReceiverThrowableType
         {
            final GaeEntityStoreUnitOfWork euow = new GaeEntityStoreUnitOfWork(datastoreService, generateId(), module);
            Query query = new Query();
            PreparedQuery q = datastoreService.prepare(query);
            final QueryResultIterable<Entity> iterable = q.asQueryResultIterable();

            output.receiveFrom(new Sender<EntityState, EntityStoreException>()
            {
               @Override
               public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super EntityState, ReceiverThrowableType> receiver) throws ReceiverThrowableType, EntityStoreException
               {
                  for (Entity entity : iterable)
                  {
                     EntityState entityState = new GaeEntityState(euow, entity, module);
                     receiver.receive(entityState);
                  }
               }
            });
         }
      };
   }

   private String generateId()
   {
      return uuid + counter++;
   }
}