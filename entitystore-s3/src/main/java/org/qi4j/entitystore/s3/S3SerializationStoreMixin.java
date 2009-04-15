/*  Copyright 2008 Rickard �berg.
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
package org.qi4j.entitystore.s3;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Streams;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.helpers.MapEntityStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Amazon S3 implementation of SerializationStore.
 * <p/>
 * To use this you must supply your own access key and secret key for your Amazon S3 account.
 */
public class S3SerializationStoreMixin
        implements MapEntityStore, Activatable
{
    private
    @This
    ReadWriteLock lock;
    private
    @This
    Configuration<S3Configuration> configuration;

    private S3Service s3Service;
    private S3Bucket entityBucket;

    // Activatable implementation
    public void activate() throws Exception
    {
        String awsAccessKey = configuration.configuration().accessKey().get();
        String awsSecretKey = configuration.configuration().secretKey().get();

        if (awsAccessKey == null || awsSecretKey == null)
        {
            throw new IllegalStateException("No S3 keys configured");
        }

        AWSCredentials awsCredentials =
                new AWSCredentials(awsAccessKey, awsSecretKey);
        s3Service = new RestS3Service(awsCredentials);

        S3Bucket[] s3Buckets = s3Service.listAllBuckets();
        System.out.println("How many buckets do I have in S3? " + s3Buckets.length);

        if (s3Buckets.length == 0)
        {
            entityBucket = s3Service.createBucket("entity-bucket");
            System.out.println("Created entity bucket: " + entityBucket.getName());
        } else
        {
            entityBucket = s3Buckets[0];
        }
    }

    public void passivate() throws Exception
    {
    }

    // MapEntityStore implementation
    public boolean contains(EntityReference entityReference)
            throws EntityStoreException
    {
        // TODO
        return false;
    }

    public void get(EntityReference entityReference, OutputStream out)
            throws EntityStoreException
    {
        try
        {
            S3Object objectComplete = s3Service.getObject(entityBucket, entityReference.toString());
            System.out.println("S3Object, complete: " + objectComplete);

            InputStream inputStream = objectComplete.getDataInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Streams.copyStream(inputStream, bout, true);
        } catch (Exception e)
        {
            throw new EntityStoreException("Could not get state", e);
        }
    }

    public void update(Map<EntityReference, InputStream> newEntities, Map<EntityReference, InputStream> updatedEntities, Iterable<EntityReference> removedEntities)
    {
        for (Map.Entry<EntityReference, InputStream> entityReferenceInputStreamEntry : newEntities.entrySet())
        {
            put(entityReferenceInputStreamEntry.getKey(), entityReferenceInputStreamEntry.getValue());
        }

        for (Map.Entry<EntityReference, InputStream> entityReferenceInputStreamEntry : updatedEntities.entrySet())
        {
            put(entityReferenceInputStreamEntry.getKey(), entityReferenceInputStreamEntry.getValue());
        }

        for (EntityReference removedEntity : removedEntities)
        {
            remove(removedEntity);
        }
    }

    public void put(EntityReference entityReference, InputStream in)
            throws EntityStoreException
    {
        try
        {
            S3Object entityState = new S3Object(entityReference.toString());
            entityState.setDataInputStream(in);
            entityState.setContentLength(in.available());
            entityState.setContentType("application/octetstream");

            s3Service.putObject(entityBucket, entityState);
        } catch (Exception e)
        {
            throw new EntityStoreException("Could not store state", e);
        }
    }

    public void remove(EntityReference removedEntity)
            throws EntityStoreException
    {
        try
        {
            s3Service.deleteObject(entityBucket, removedEntity.toString());
        } catch (S3ServiceException e)
        {
            throw new EntityStoreException("Could not remove state", e);
        }
    }

    public void visitMap(MapEntityStoreVisitor visitor)
    {
        try
        {
            final S3Object[] entities = s3Service.listObjects(entityBucket);
            for (S3Object entity : entities)
            {
                visitor.visitEntity(entity.getDataInputStream());
            }
        } catch (S3ServiceException e)
        {
            throw new EntityStoreException(e);
        }
    }
}