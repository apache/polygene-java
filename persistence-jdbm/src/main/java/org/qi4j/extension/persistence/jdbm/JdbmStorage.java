/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.extension.persistence.jdbm;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.persistence.ObjectNotFoundException;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.api.persistence.PersistentStorage;
import org.qi4j.api.persistence.binding.PersistenceBinding;
import org.qi4j.runtime.ObjectInvocationHandler;
import org.qi4j.spi.object.ProxyReferenceInvocationHandler;


public class JdbmStorage
    implements PersistentStorage, XAResource
{
    private ObjectFactory objectFactory;
    private TransactionManager transactionManager;
    private HashMap<Transaction, TransactionBuffer> transactions;
    private RecordManager recordManager;
    private HashMap<Xid, Transaction> xids;

    public JdbmStorage( ObjectFactory factory, File directory, TransactionManager theTransactionManager )
        throws IOException
    {
        objectFactory = factory;
        transactionManager = theTransactionManager;
        transactions = new HashMap<Transaction, TransactionBuffer>();
        xids = new HashMap<Xid, Transaction>();

        String name = new File( directory, "qi4j.data" ).getAbsolutePath();
        Properties properties;
        try
        {
            properties = getProperties( directory );
        }
        catch( IOException e )
        {
            throw new PersistenceException( "Unable to read properties from " + directory + "/qi4j.properties", e );
        }
        recordManager = RecordManagerFactory.createRecordManager( name, properties );
    }

    public void create( PersistenceBinding aProxy )
        throws PersistenceException
    {
        try
        {
            Transaction transaction = transactionManager.getTransaction();
            TransactionBuffer transactionBuffer = transactions.get( transaction );
            transactionBuffer.create( aProxy );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
    }

    public void read( PersistenceBinding aProxy )
        throws PersistenceException
    {
        try
        {
            Transaction transaction = transactionManager.getTransaction();
            TransactionBuffer transactionBuffer = transactions.get( transaction );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }

        String objectId = aProxy.getIdentity();
        try
        {
            long recordId = recordManager.getNamedObject( objectId );
            Map<Class, Serializable> mixins = (Map<Class, Serializable>) recordManager.fetch( recordId );
            if( mixins == null )
            {
                throw new ObjectNotFoundException( "Object with identity " + objectId + " does not exist" );
            }

            ProxyReferenceInvocationHandler proxyHandler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( aProxy );
            ObjectInvocationHandler handler = ObjectInvocationHandler.getInvocationHandler( objectFactory.getThat( aProxy ) );
            Map<Class, Object> existingMixins = handler.getMixins();
            existingMixins.putAll( mixins );
            proxyHandler.initializeMixins( existingMixins );
            recordManager.commit();
        }
        catch( EOFException e )
        {
            try
            {
                recordManager.rollback();
            }
            catch( IOException e1 )
            {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            throw new ObjectNotFoundException( "Object with identity " + objectId + " does not exist" );
        }
        // Possible Exceptions
//        catch( IOException e )
//        {
//            throw new PersistenceException( e );
//        }
//        catch( IllegalAccessException e )
//        {
//            throw new PersistenceException( e );
//        }
//        catch( InvocationTargetException e )
//        {
//            throw new PersistenceException( e );
//        }
//        catch( IntrospectionException e )
//        {
//            throw new PersistenceException( e );
//        }
        catch( Exception e )
        {
            try
            {
                recordManager.rollback();
            }
            catch( IOException e1 )
            {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            throw new PersistenceException( e );
        }
    }

    public void update( PersistenceBinding aProxy, Serializable aMixin )
        throws PersistenceException
    {
        try
        {
            Transaction transaction = transactionManager.getTransaction();
            TransactionBuffer transactionBuffer = transactions.get( transaction );
            transactionBuffer.update( aProxy, aMixin );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
    }

    public void delete( PersistenceBinding aProxy )
        throws PersistenceException
    {
        try
        {
            Transaction transaction = transactionManager.getTransaction();
            TransactionBuffer transactionBuffer = transactions.get( transaction );
            transactionBuffer.delete( aProxy );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
    }

    public void enlistResource() throws SystemException, RollbackException
    {
        if( transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION )
        {
            Transaction transaction = transactionManager.getTransaction();
            transaction.enlistResource( this );
        }
    }

    private Properties getProperties( File directory )
        throws IOException
    {
        Properties properties = new Properties();
        File propertiesFile = new File( directory, "qi4j.properties" );
        if( propertiesFile.exists() )
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( propertiesFile );
                BufferedInputStream bis = new BufferedInputStream( fis );
                properties.load( bis );
            }
            finally
            {
                if( fis != null )
                {
                    fis.close();
                }
            }
        }
        return properties;
    }

    // XAResource Implementation -----

    public void commit( Xid xid, boolean onePhase )
        throws XAException
    {
        if( onePhase )
        {
            onePhaseCommit( xid );
            return;
        }
        twoPhaseCommitPhase2( xid );
    }

    private void twoPhaseCommitPhase2( Xid xid )
    {
        // Phase 1 == prepare();

    }

    private void onePhaseCommit( Xid xid )
        throws XAException
    {
        Transaction tx = xids.get( xid );
        TransactionBuffer buffer = transactions.get( tx );
        try
        {
            buffer.commit( recordManager );
        }
        catch( IOException e )
        {
            // TODO: What to return?
            throw new XAException();
        }
    }

    public void end( Xid xid, int flags )
        throws XAException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void forget( Xid xid )
        throws XAException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getTransactionTimeout()
        throws XAException
    {
        return 0;
    }

    public boolean isSameRM( XAResource xaResource )
        throws XAException
    {
        return xaResource == this;
    }

    public int prepare( Xid xid )
        throws XAException
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Xid[] recover( int flag )
        throws XAException
    {
        return new Xid[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void rollback( Xid xid )
        throws XAException
    {

    }

    public boolean setTransactionTimeout( int seconds )
        throws XAException
    {
        return false;
    }

    public void start( Xid xid, int flags )
        throws XAException
    {
        try
        {
            if( ( flags & TMJOIN ) == TMJOIN )
            {
                join( xid );
                return;
            }
            if( ( flags & TMRESUME ) == TMRESUME )
            {
                resume( xid );
                return;
            }
            Transaction transaction = transactionManager.getTransaction();
            TransactionBuffer trm = new TransactionBuffer( xid );
            xids.put( xid, transaction );
            transactions.put( transaction, trm );
        }
        catch( SystemException e )
        {
            // TODO: What shall we do here?
            throw new XAException( e.getMessage() );
        }
    }

    private void join( Xid xid )
    {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void resume( Xid xid )
    {

    }
}
