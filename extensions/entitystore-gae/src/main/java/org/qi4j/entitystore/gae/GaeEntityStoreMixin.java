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

import com.google.appengine.api.datastore.*;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;

/**
 * GAE implementation of SerializationStore
 */
public class GaeEntityStoreMixin
        implements GaeEntityStoreActivation, EntityStore
{
   private final ValueSerialization valueSerialization;
   private final String uuid;
   private long counter;
   private DatastoreService datastoreService;

   public GaeEntityStoreMixin( @Service IdentityGenerator uuid,
                               @Service @Tagged( ValueSerialization.Formats.JSON ) ValueSerialization valueSerialization )
   {
      System.out.println("Initializing GAE EntityStore.");
      this.uuid = uuid.generate(Identity.class) + ":";
      this.valueSerialization = valueSerialization;
      counter = 0L;
   }

   @Override
   public void activateGaeEntityStore()
           throws Exception
   {
      datastoreService = DatastoreServiceFactory.getDatastoreService();
   }

    @Override
   public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module, long currentTime )
   {
       return new GaeEntityStoreUnitOfWork( datastoreService, valueSerialization, generateId(), module, currentTime );
   }

    @Override
   public Input<EntityState, EntityStoreException> entityStates(final Module module)
   {
      return new Input<EntityState, EntityStoreException>()
      {
         @Override
         public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super EntityState, ReceiverThrowableType> output) throws EntityStoreException, ReceiverThrowableType
         {
            final GaeEntityStoreUnitOfWork euow = new GaeEntityStoreUnitOfWork( datastoreService, valueSerialization,
                                                                                generateId(), module,
                                                                                System.currentTimeMillis() );
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
                     EntityState entityState = new GaeEntityState( euow, valueSerialization, entity, module );
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