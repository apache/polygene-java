/*  Copyright 2008 Rickard …berg.
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
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.CompositeInputStream;
import org.qi4j.spi.serialization.CompositeOutputStream;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.serialization.SerializationStore;
import org.qi4j.spi.serialization.SerializedState;

/**
 * Amazon S3 implementation of SerializationStore.
 * <p/>
 * To use this you must supply your own access key and secret key for your Amazon S3 account.
 */
public class S3SerializationStoreMixin
    implements SerializationStore, Activatable
{
    private @Structure Qi4jSPI spi;
    private @ThisCompositeAs ReadWriteLock lock;

    private S3Service s3Service;
    private S3Bucket entityBucket;

    // Activatable implementation
    public void activate() throws Exception
    {
        String awsAccessKey = null;
        String awsSecretKey = null;
//      awsAccessKey = "INSERT-YOUR-KEY-HERE";
//      awsSecretKey = "INSERT-YOUR-SECRET-KEY-HERE";

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

    // SerializationStore implementation
    @WriteLock
    public SerializedState get( EntityId entityIdId, UnitOfWork unitOfWork ) throws IOException
    {
        try
        {
            S3Object objectComplete = s3Service.getObject( entityBucket, entityIdId.toString() );
            System.out.println( "S3Object, complete: " + objectComplete );

            InputStream inputStream = objectComplete.getDataInputStream();
            CompositeInputStream stream = new CompositeInputStream( inputStream, unitOfWork, spi );
            SerializedState state = (SerializedState) stream.readObject();

            return state;
        }
        catch( S3ServiceException e )
        {
            if( e.getS3ErrorCode().equals( "NoSuchKey" ) )
            {
                return null;
            }
            throw (IOException) new IOException().initCause( e );
        }
        catch( ClassNotFoundException e )
        {
            throw (IOException) new IOException().initCause( e );
        }
    }

    @WriteLock
    public boolean contains( EntityId entityIdId ) throws IOException
    {
        try
        {
            S3Object entityDetailsOnly = s3Service.getObjectDetails( entityBucket, entityIdId.toString() );
            return true;
        }
        catch( S3ServiceException e )
        {
            if( e.getMessage().indexOf( "ResponseCode=404" ) != -1 ) // Hack, but seems to work
            {
                return false;
            }
            throw (IOException) new IOException().initCause( e );
        }
    }

    public StateCommitter prepare( Map<EntityId, SerializedState> newEntities, Map<EntityId, SerializedState> updatedEntities, Iterable<EntityId> removedEntities )
        throws IOException
    {
        lock.writeLock().lock();

        try
        {
            // Add state
            for( Map.Entry<EntityId, SerializedState> entry : newEntities.entrySet() )
            {
                uploadObject( entry );
            }

            // Update state
            for( Map.Entry<EntityId, SerializedState> entry : updatedEntities.entrySet() )
            {
                uploadObject( entry );
            }

            // Remove state
            for( EntityId removedEntityId : removedEntities )
            {
                s3Service.deleteObject( entityBucket, removedEntityId.toString() );
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
        catch( S3ServiceException e )
        {
            lock.writeLock().unlock();
            throw (IOException) new IOException().initCause( e );
        }
    }

    private void uploadObject( Map.Entry<EntityId, SerializedState> entry )
        throws IOException, S3ServiceException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream( 1024 );
        CompositeOutputStream stream = new CompositeOutputStream( out );
        stream.writeObject( entry.getValue() );
        stream.flush();
        byte[] data = out.toByteArray();
        stream.close();
        out.close();

        S3Object entityState = new S3Object( entry.getKey().toString() );
        ByteArrayInputStream entityData = new ByteArrayInputStream( data );
        entityState.setDataInputStream( entityData );
        entityState.setContentLength( entityData.available() );
        entityState.setContentType( "application/octetstream" );

        s3Service.putObject( entityBucket, entityState );
    }
}