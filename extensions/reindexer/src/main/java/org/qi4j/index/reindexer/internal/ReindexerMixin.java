/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.index.reindexer.internal;

import java.util.ArrayList;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexerMixin
        implements Reindexer
{
   private static QualifiedName identityQN;

   static
   {
      try
      {
         identityQN = QualifiedName.fromAccessor( Identity.class.getMethod( "identity" ) );
      } catch (NoSuchMethodException e)
      {
         throw new InternalError("Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: ReindexerMixin");
      }
   }

   @This
   private Configuration<ReindexerConfiguration> configuration;

   @Service
   private EntityStore store;
   @Service
   private Iterable<ServiceReference<StateChangeListener>> listeners;
   @Structure
   private Module module;

   private Logger logger = LoggerFactory.getLogger(Reindexer.class);

    @Override
   public void reindex()
   {
      configuration.refresh();
      ReindexerConfiguration conf = configuration.get();
      Integer loadValue = conf.loadValue().get();
      if (loadValue == null)
      {
         loadValue = 50;
      }
      new ReindexerOutput(loadValue).reindex(store);
   }

   private class ReindexerOutput
           implements Output<EntityState, RuntimeException>, Receiver<EntityState, RuntimeException>
   {
      private int count;
      private int loadValue;
      private ArrayList<EntityState> states;

      public ReindexerOutput(Integer loadValue)
      {
         this.loadValue = loadValue;
         states = new ArrayList<EntityState>();
      }

      public void reindex(EntityStore store)
      {

         store.entityStates(module).transferTo(this);
         reindexState();
      }

      @Override
      public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends EntityState, SenderThrowableType> sender) throws RuntimeException, SenderThrowableType
      {
         sender.sendTo(this);
         reindexState();
      }

       @Override
      public void receive(EntityState item)
              throws RuntimeException
      {
         count++;
         item.setPropertyValue( identityQN, item.identity().identity() );
         states.add(item);

         if (states.size() >= loadValue)
         {
            reindexState();
         }
      }

      public void reindexState()
      {
         for (ServiceReference<StateChangeListener> listener : listeners)
         {
            listener.get().notifyChanges(states);
         }
         states.clear();
         logger.debug("Reindexed " + count + " entities");
      }
   }
}
