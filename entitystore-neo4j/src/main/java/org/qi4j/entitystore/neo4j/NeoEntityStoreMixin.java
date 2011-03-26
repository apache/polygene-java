package org.qi4j.entitystore.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.*;
import org.qi4j.spi.structure.ModuleSPI;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NeoEntityStoreMixin
        implements Activatable, EntityStore, EntityStoreSPI
{
   @Optional
   @Service
   FileConfiguration fileConfiguration;

   @This
   private Configuration<NeoConfiguration> config;

   private EmbeddedGraphDatabase neo;
   private IndexService indexService;

   private AtomicInteger count = new AtomicInteger(0);
   private String uuid;

   public void activate()
           throws Exception
   {
      String path = config.configuration().path().get();
      if (path == null)
      {
         if (fileConfiguration != null)
            path = new File(fileConfiguration.dataDirectory(), config.configuration().identity().get()).getAbsolutePath();
         else
            path = "build/neodb";
      }
      neo = new EmbeddedGraphDatabase(path);
      indexService = new LuceneIndexService(neo);
      uuid = UUID.randomUUID().toString() + "-";
   }

   public void passivate()
           throws Exception
   {
      indexService.shutdown();
      neo.shutdown();
   }

   public EntityStoreUnitOfWork newUnitOfWork(Usecase usecase, ModuleSPI module)
   {
      return new NeoEntityStoreUnitOfWork(neo, indexService, newUnitOfWorkId(), module);
   }

   public Input<EntityState, EntityStoreException> entityStates(final ModuleSPI module)
   {
      return new Input<EntityState, EntityStoreException>()
      {
         @Override
         public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super EntityState, ReceiverThrowableType> output) throws EntityStoreException, ReceiverThrowableType
         {
            output.receiveFrom(new Sender<EntityState, EntityStoreException>()
            {
               @Override
               public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super EntityState, ReceiverThrowableType> receiver) throws ReceiverThrowableType, EntityStoreException
               {
                  NeoEntityStoreUnitOfWork uow = new NeoEntityStoreUnitOfWork(neo, indexService, newUnitOfWorkId(), module);

                  try
                  {
                     Iterable<Relationship> relationships =
                             neo.getReferenceNode().getRelationships(RelTypes.ENTITY_TYPE_REF, Direction.OUTGOING);

                     for (Relationship entityTypeRel : relationships)
                     {
                        Node entityType = entityTypeRel.getEndNode();
                        for (Relationship entityRel : entityType.getRelationships(RelTypes.IS_OF_TYPE, Direction.INCOMING))
                        {
                           Node entityNode = entityRel.getStartNode();
                           NeoEntityState entityState = new NeoEntityState(uow, entityNode, EntityStatus.LOADED);
                           receiver.receive(entityState);
                        }
                     }
                  } finally
                  {
                     uow.discard();
                  }
               }
            });
         }
      };
   }

   public StateCommitter applyChanges(EntityStoreUnitOfWork unitofwork, Iterable<EntityState> state,
                                      String version, long lastModified)
   {
      for (EntityState firstState : state)
      {
         if (firstState instanceof NeoEntityState)
         {
            return ((NeoEntityState) firstState).getUnitOfWork().applyChanges();
         }
      }
      return null;
   }

   public EntityState getEntityState(EntityStoreUnitOfWork unitOfWork, EntityReference identity)
   {
      return unitOfWork.getEntityState(identity);
   }

   public EntityState newEntityState(EntityStoreUnitOfWork uow, EntityReference ref, EntityDescriptor descriptor)
   {
      return uow.newEntityState(ref, descriptor);
   }

   private String newUnitOfWorkId()
   {
      return uuid + Integer.toHexString(count.incrementAndGet());
   }
}