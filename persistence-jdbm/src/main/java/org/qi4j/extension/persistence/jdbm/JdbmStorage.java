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
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
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
    implements PersistentStorage
{
    private ObjectFactory objectFactory;
    private TransactionManager transactionManager;
    private RecordManager recordManager;
    private HashMap<Transaction, TransactionResource> transactions;

    public JdbmStorage( ObjectFactory factory, File directory, TransactionManager theTransactionManager )
        throws IOException
    {
        objectFactory = factory;
        transactionManager = theTransactionManager;
        transactions = new HashMap<Transaction, TransactionResource>();

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
            TransactionResource transactionResource = getTransactionResource();
            transactionResource.create( aProxy );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
        catch( RollbackException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
    }

    public void read( PersistenceBinding aProxy )
        throws PersistenceException
    {
        try
        {
            TransactionResource transactionResource = getTransactionResource();
            transactionResource.read( aProxy );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
        catch( RollbackException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }

    }

    public void update( PersistenceBinding aProxy, Serializable aMixin )
        throws PersistenceException
    {
        try
        {
            TransactionResource transactionResource = getTransactionResource();
            transactionResource.update( aProxy, aMixin );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
        catch( RollbackException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
    }

    public void delete( PersistenceBinding aProxy )
        throws PersistenceException
    {
        try
        {
            TransactionResource transactionResource = getTransactionResource();
            transactionResource.delete( aProxy );
        }
        catch( SystemException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
        catch( RollbackException e )
        {
            throw new TransactionSystemException( "TransactionManager failure.", e );
        }
    }

    private TransactionResource getTransactionResource()
        throws SystemException, RollbackException
    {
        Transaction transaction = transactionManager.getTransaction();
        TransactionResource transactionResource = transactions.get( transaction );
        if( transactionResource == null )
        {
            transactionResource = new TransactionResource( recordManager, objectFactory );
            transaction.enlistResource( transactionResource );
            transactions.put( transaction, transactionResource );
        }
        return transactionResource;
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
}
