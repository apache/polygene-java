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
package org.qi4j.entity.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.qi4j.injection.scope.This;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.spi.entity.AbstractEntityStoreMixin;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.SerializableState;

/**
 * Amazon S3 implementation of SerializationStore.
 * <p/>
 * To use this you must supply your own access key and secret key for your Amazon S3 account.
 */
public class S3SerializationStoreMixin
    extends AbstractEntityStoreMixin
    implements Activatable
{
    private @This ReadWriteLock lock;
    private @This Configuration<S3Configuration> configuration;

    private S3Service s3Service;
    private S3Bucket entityBucket;

    // Activatable implementation
    public void activate() throws Exception
    {
        String awsAccessKey = configuration.configuration().accessKey().get();
        String awsSecretKey = configuration.configuration().secretKey().get();

        if( awsAccessKey == null || awsSecretKey == null )
        {
            throw new IllegalStateException( "No S3 keys configured" );
        }

        AWSCredentials awsCredentials =
            new AWSCredentials( awsAccessKey, awsSecretKey );
        s3Service = new RestS3Service( awsCredentials );

        S3Bucket[] s3Buckets = s3Service.listAllBuckets();
        System.out.println( "How many buckets do I have in S3? " + s3Buckets.length );

        if( s3Buckets.length == 0 )
        {
            entityBucket = s3Service.createBucket( "entity-bucket" );
            System.out.println( "Created entity bucket: " + entityBucket.getName() );
        }
        else
        {
            entityBucket = s3Buckets[ 0 ];
        }
    }

    public void passivate() throws Exception
    {
    }

    // EntityStore implementation
    public void registerEntityType( EntityType entityType )
    {
    }

    @WriteLock
    public EntityState newEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        // Skip existence check
        return new DefaultEntityState( identity, getEntityType( identity ) );
    }

    @WriteLock
    public EntityState getEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity );
        try
        {
            S3Object objectComplete = s3Service.getObject( entityBucket, identity.identity() );
            System.out.println( "S3Object, complete: " + objectComplete );

            InputStream inputStream = objectComplete.getDataInputStream();
            ObjectInputStream stream = new ObjectInputStream( inputStream );
            SerializableState serializableState = (SerializableState) stream.readObject();

            return new DefaultEntityState( serializableState.version(),
                                           serializableState.lastModified(),
                                           identity,
                                           EntityStatus.LOADED,
                                           entityType,
                                           serializableState.properties(),
                                           serializableState.associations(),
                                           serializableState.manyAssociations() );
        }
        catch( S3ServiceException e )
        {
            if( e.getS3ErrorCode().equals( "NoSuchKey" ) )
            {
                throw new EntityNotFoundException( "S3 store", identity.identity() );
            }
            throw new EntityStoreException( e );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( e );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
    {
        lock.writeLock().lock();

        try
        {
            long lastModified = System.currentTimeMillis();
            for( EntityState entityState : newStates )
            {
                DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
                SerializableState state = new SerializableState( entityState.qualifiedIdentity(),
                                                                 entityState.version(),
                                                                 lastModified,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                uploadObject( entityState.qualifiedIdentity(), state );
            }

            for( EntityState entityState : loadedStates )
            {
                DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
                SerializableState state = new SerializableState( entityState.qualifiedIdentity(),
                                                                 entityState.version(),
                                                                 lastModified,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                uploadObject( entityState.qualifiedIdentity(), state );
            }

            for( QualifiedIdentity removedState : removedStates )
            {
                s3Service.deleteObject( entityBucket, removedState.identity() );
            }

            return new StateCommitter()
            {
                public void commit()
                {
                    lock.writeLock().unlock();
                }

                public void cancel()
                {

                    lock.writeLock().unlock();
                }
            };
        }
        catch( Exception e )
        {
            lock.writeLock().unlock();
            throw new EntityStoreException( e );
        }
    }

    public Iterator<EntityState> iterator()
    {
        return null;
    }

    private void uploadObject( QualifiedIdentity identity, SerializableState serializableState )
        throws IOException, S3ServiceException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream( 1024 );
        ObjectOutputStream stream = new ObjectOutputStream( out );
        stream.writeObject( serializableState );
        stream.flush();
        byte[] data = out.toByteArray();
        stream.close();
        out.close();

        S3Object entityState = new S3Object( identity.identity() );
        ByteArrayInputStream entityData = new ByteArrayInputStream( data );
        entityState.setDataInputStream( entityData );
        entityState.setContentLength( entityData.available() );
        entityState.setContentType( "application/octetstream" );

        s3Service.putObject( entityBucket, entityState );
    }
}