/*  Copyright 2010 Niclas Hedhman.
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
package org.qi4j.entitystore.voldemort;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.service.Activatable;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.service.ServiceDescriptor;
import voldemort.client.ClientConfig;
import voldemort.client.RoutingTier;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.client.protocol.RequestFormatType;
import voldemort.versioning.ObsoleteVersionException;
import voldemort.versioning.Versioned;

/**
 * JDBM implementation of SerializationStore
 */
public class VoldemortEntityStoreMixin
        implements Activatable, MapEntityStore
{
   @This
   private ReadWriteLock lock;

   @This
   private Configuration<VoldemortConfiguration> config;

   private StoreClient<String, byte[]> client;
   private StoreClientFactory factory;

   // Activatable implementation

   public void activate()
           throws Exception
   {
      VoldemortConfiguration conf = config.configuration();
      ClientConfig config = new ClientConfig();
      {
         List<String> value = conf.bootstrapUrl().get();
         if (value != null)
         {
            config.setBootstrapUrls(value);
         } else
         {
            config.setBootstrapUrls("tcp://localhost:8581");
         }
      }
      {
         Integer connectionTimeout = conf.connectionTimeout().get();
         if (connectionTimeout != null)
         {
            config.setConnectionTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
         }
      }
      {
         Boolean enable = conf.enableJmx().get();
         if (enable != null)
         {
            config.setEnableJmx(enable);
         }
      }
      {
         Long recoveryInterval = conf.failureDetectorAsyncRecoveryInterval().get();
         if (recoveryInterval != null)
         {
            config.setFailureDetectorAsyncRecoveryInterval(recoveryInterval);
         }
      }
      {
         Long bannagePeriod = conf.failureDetectorBannagePeriod().get();
         if (bannagePeriod != null)
         {
            config.setFailureDetectorBannagePeriod(bannagePeriod);
         }
      }
      {
         List<String> failureDetectorCatastrophicErrorTypes = conf.failureDetectorCatastrophicErrorType().get();
         if (failureDetectorCatastrophicErrorTypes != null)
         {
            config.setFailureDetectorCatastrophicErrorTypes(failureDetectorCatastrophicErrorTypes);
         }
      }
      {
         String failureDetectorImplementation = conf.failureDetectorImplementation().get();
         if (failureDetectorImplementation != null)
         {
            config.setFailureDetectorImplementation(failureDetectorImplementation);
         }
      }
      {
         Long failureDetectorRequestLengthThreshold = conf.failureDetectoreRequestLengthThreshold().get();
         if (failureDetectorRequestLengthThreshold != null)
         {
            config.setFailureDetectorRequestLengthThreshold(failureDetectorRequestLengthThreshold);
         }
      }
      {
         Integer failureDetectorThreshold = conf.failureDetectorThreshold().get();
         if (failureDetectorThreshold != null)
         {
            config.setFailureDetectorThreshold(failureDetectorThreshold);
         }
      }
      {
         Integer detectorThresholdCountMinimum = conf.failureDetectorThresholdCountMinimum().get();
         if (detectorThresholdCountMinimum != null)
         {
            config.setFailureDetectorThresholdCountMinimum(detectorThresholdCountMinimum);
         }
      }
      {
         Long failureDetectorThresholdInterval = conf.failureDetectorThreasholdInterval().get();
         if (failureDetectorThresholdInterval != null)
         {
            config.setFailureDetectorThresholdInterval(failureDetectorThresholdInterval);
         }
      }
      {
         Integer maxBootstrapRetries = conf.maxBootstrapRetries().get();
         if (maxBootstrapRetries != null)
         {
            config.setMaxBootstrapRetries(maxBootstrapRetries);
         }
      }
      {
         Integer maxConnectionsPerNode = conf.setMaxConnectionsPerNode().get();
         if (maxConnectionsPerNode != null)
         {
            config.setMaxConnectionsPerNode(maxConnectionsPerNode);
         }
      }
      {
         Integer maxQueueRequests = conf.maxQueueRequests().get();
         if (maxQueueRequests != null)
         {
            config.setMaxQueuedRequests(maxQueueRequests);
         }
      }
      {
         Integer maxThreads = conf.maxThreads().get();
         if (maxThreads != null)
         {
            config.setMaxThreads(maxThreads);
         }
      }
      {
         Integer maxTotalConnections = conf.maxTotalConnections().get();
         if (maxTotalConnections != null)
         {
            config.setMaxTotalConnections(maxTotalConnections);
         }
      }
      {
         String formatTypeCode = conf.requestFormatType().get();
         if (formatTypeCode != null)
         {
            RequestFormatType formatType = RequestFormatType.fromCode(formatTypeCode);
            config.setRequestFormatType(formatType);
         }
      }
      {
         String routingTierString = conf.routingTier().get();
         if (routingTierString != null)
         {
            RoutingTier routingTier = RoutingTier.fromDisplay(routingTierString);
            config.setRoutingTier(routingTier);
         }
      }
      {
         Integer routingTimeout = conf.routingTimeout().get();
         if (routingTimeout != null)
         {
            config.setRoutingTimeout(routingTimeout, TimeUnit.MILLISECONDS);
         }
      }
      {
         Integer bufferSize = conf.socketBufferSize().get();
         if (bufferSize != null)
         {
            config.setSocketBufferSize(bufferSize);
         }
      }
      {
         Boolean socketKeepAlive = conf.socketKeepAlive().get();
         if (socketKeepAlive != null)
         {
            config.setSocketKeepAlive(socketKeepAlive);
         }
      }
      {
         Integer socketTimeout = conf.socketTimeout().get();
         if (socketTimeout != null)
         {
            config.setSocketTimeout(socketTimeout, TimeUnit.MILLISECONDS);
         }
      }
      {
         Integer idleTime = conf.threadIdleTime().get();
         if (idleTime != null)
         {
            config.setThreadIdleTime(idleTime, TimeUnit.MILLISECONDS);
         }
      }
      factory = new SocketStoreClientFactory(config);
      // create a client that executes operations on a single store
      String storeName = conf.storeName().get();
      if (storeName == null)
         storeName = "qi4j-entities";
      client = factory.getStoreClient(storeName);

   }

   public void passivate()
           throws Exception
   {
      factory.close();
   }

   public Reader get(EntityReference entityReference)
           throws EntityStoreException
   {
      try
      {
         Versioned<byte[]> versioned = client.get(entityReference.identity());
         if (versioned == null)
         {
            throw new EntityNotFoundException(entityReference);
         }
         byte[] serializedState = versioned.getValue();
         return new StringReader(new String(serializedState, "UTF-8"));
      } catch (IOException e)
      {
         throw new EntityStoreException(e);
      }
   }

   public void applyChanges(MapChanges changes)
           throws IOException
   {
      try
      {
         changes.visitMap(new MapChanger()
         {
            public Writer newEntity(final EntityReference ref, EntityType entityType)
                    throws IOException
            {
               return new StringWriter(1000)
               {
                  @Override
                  public void close()
                          throws IOException
                  {
                     super.close();
                     byte[] stateArray = toString().getBytes("UTF-8");
                     client.put(ref.identity(), stateArray);
                  }
               };
            }

            public Writer updateEntity(final EntityReference ref, EntityType entityType)
                    throws IOException
            {
               return new StringWriter(1000)
               {
                  @Override
                  public void close()
                          throws IOException
                  {
                     super.close();
                     byte[] stateArray = toString().getBytes("UTF-8");
                     try
                     {
                        client.put(ref.identity(), stateArray);
                     } catch (ObsoleteVersionException e)
                     {
                        throw new ConcurrentModificationException(
                                "Concurrent modification attempted for " + ref.identity());
                     }
                  }
               }

                       ;
            }

            public void removeEntity(EntityReference ref, EntityType entityType)
                    throws EntityNotFoundException
            {
               client.delete(ref.identity());
            }
         });
      } catch (Exception e)
      {
         if (e instanceof IOException)
         {
            throw (IOException) e;
         } else if (e instanceof EntityStoreException)
         {
            throw (EntityStoreException) e;
         } else
         {
            IOException exception = new IOException();
            exception.initCause(e);
            throw exception;
         }
      }
   }

   public Input<Reader, IOException> entityStates()
   {
      return new Input<Reader, IOException>()
      {
         @Override
         public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super Reader, ReceiverThrowableType> receiverThrowableTypeOutput) throws IOException, ReceiverThrowableType
         {
            // TODO: Can't get hold of all entities, unless storing all the keys separately, which is enormously expensive
         }
      };
   }
}