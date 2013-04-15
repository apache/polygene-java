package org.qi4j.entitystore.neo4j;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.*;

public class NeoEntityStoreMixin
        implements ServiceActivation, EntityStore, EntityStoreSPI
{
   @Optional
   @Service
   FileConfiguration fileConfiguration;
   @Service
   @Tagged( ValueSerialization.Formats.JSON )
   private ValueSerialization valueSerialization;

   @This
   private Configuration<NeoConfiguration> config;

   private EmbeddedGraphDatabase neo;
   private IndexService indexService;

   private AtomicInteger count = new AtomicInteger(0);
   private String uuid;

   @Override
   public void activateService()
           throws Exception
   {
      String path = config.get().path().get();
      if (path == null)
      {
         if (fileConfiguration != null)
            path = new File(fileConfiguration.dataDirectory(), config.get().identity().get()).getAbsolutePath();
         else
            path = "build/neodb";
      }
      neo = new EmbeddedGraphDatabase(path);
      indexService = new LuceneIndexService(neo);
      uuid = UUID.randomUUID().toString() + "-";
   }

   @Override
   public void passivateService()
           throws Exception
   {
      indexService.shutdown();
      neo.shutdown();
   }

    @Override
   public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module, long currentTime )
   {
      return new NeoEntityStoreUnitOfWork(neo, indexService, valueSerialization, newUnitOfWorkId(), module, currentTime);
   }

    @Override
   public Input<EntityState, EntityStoreException> entityStates(final Module module)
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
                  NeoEntityStoreUnitOfWork uow = new NeoEntityStoreUnitOfWork(neo, indexService, valueSerialization, newUnitOfWorkId(), module, System.currentTimeMillis());

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
                           NeoEntityState entityState = new NeoEntityState( valueSerialization, uow, entityNode, EntityStatus.LOADED);
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

    @Override
   public StateCommitter applyChanges( EntityStoreUnitOfWork unitofwork, Iterable<EntityState> state )
   {
      for (EntityState firstState : state)
      {
         if (firstState instanceof NeoEntityState)
         {
            return ((NeoEntityState) firstState).unitOfWork().applyChanges();
         }
      }
      return null;
   }

    @Override
   public EntityState entityStateOf( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
   {
      return unitOfWork.entityStateOf( identity );
   }

    @Override
   public EntityState newEntityState( EntityStoreUnitOfWork uow, EntityReference ref, EntityDescriptor descriptor )
   {
      return uow.newEntityState(ref, descriptor);
   }

   private String newUnitOfWorkId()
   {
      return uuid + Integer.toHexString(count.incrementAndGet());
   }
}