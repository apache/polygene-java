/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.spaces.javaspaces;

import java.io.IOException;
import java.io.Serializable;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;
import net.jini.config.ConfigurationFile;
import net.jini.config.ConfigurationException;
import org.qi4j.injection.scope.This;
import org.qi4j.library.spaces.Space;
import org.qi4j.library.spaces.SpaceException;
import org.qi4j.library.spaces.SpaceTransaction;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;

public class JavaSpacesClientMixin
    implements Space, Activatable
{
    @This private Configuration<JavaSpacesClientConfiguration> my;
    private ServiceHolder<JavaSpace05> spaceService;
    private ServiceHolder<TransactionManager> transactionService;
    private StackThreadLocal<TransactionProxy> transactionStack;

    public JavaSpacesClientMixin()
    {
        transactionStack = new StackThreadLocal<TransactionProxy>();
    }

    public void write( String id, Serializable entry )
    {
        synchronized( this )
        {
            if( spaceService.service != null )
            {
                try
                {
                    StorageEntry storageEntry = new StorageEntry( id, entry );
                    spaceService.service.write( storageEntry, currentTransaction(), 6000 );
                }
                catch( TransactionException e )
                {
                    e.printStackTrace();  // Can not happen without own transaction.
                }
                catch( RemoteException e )
                {
                    throw new SpaceException( "Remote problems: " + e.getMessage(), e );
                }
            }
        }
    }

    public Serializable take( String id, long timeout )
    {
        synchronized( this )
        {
            if( spaceService.service != null )
            {
                try
                {
                    StorageEntry entry = (StorageEntry) spaceService.service.take( new StorageEntry( id ), currentTransaction(), 6000 );
                    if( entry == null )
                    {
                        return null;
                    }
                    return entry.data();
                }
                catch( TransactionException e )
                {
                    e.printStackTrace();  // Can not happen without own transaction.
                }
                catch( RemoteException e )
                {
                    throw new SpaceException( "Remote problems: " + e.getMessage(), e );
                }
                catch( UnusableEntryException e )
                {
                    e.printStackTrace();
                }
                catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Serializable takeIfExists( String id )
    {
        synchronized( this )
        {
            if( spaceService.service != null )
            {
                try
                {
                    StorageEntry entry = (StorageEntry) spaceService.service.takeIfExists( new StorageEntry( id ), currentTransaction(), 6000 );
                    if( entry == null )
                    {
                        return null;
                    }
                    return entry.data();
                }
                catch( TransactionException e )
                {
                    e.printStackTrace();  // Can not happen without own transaction.
                }
                catch( RemoteException e )
                {
                    throw new SpaceException( "Remote problems: " + e.getMessage(), e );
                }
                catch( UnusableEntryException e )
                {
                    e.printStackTrace();
                }
                catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public Serializable read( String id, long timeout )
    {
        synchronized( this )
        {
            if( spaceService.service != null )
            {
                try
                {
                    StorageEntry entry = (StorageEntry) spaceService.service.read( new StorageEntry( id ), currentTransaction(), timeout );
                    if( entry == null )
                    {
                        return null;
                    }
                    return entry.data();
                }
                catch( TransactionException e )
                {
                    e.printStackTrace();  // Can not happen without own transaction.
                }
                catch( RemoteException e )
                {
                    throw new SpaceException( "Remote problems: " + e.getMessage(), e );
                }
                catch( UnusableEntryException e )
                {
                    e.printStackTrace();
                }
                catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Serializable readIfExists( String id )
    {
        synchronized( this )
        {
            if( spaceService.service != null )
            {

                try
                {
                    StorageEntry entry = (StorageEntry) spaceService.service.readIfExists( new StorageEntry( id ), currentTransaction(), 6000 );
                    if( entry == null )
                    {
                        return null;
                    }
                    return entry.data();
                }
                catch( TransactionException e )
                {
                    e.printStackTrace();  // Can not happen without own transaction.
                }
                catch( RemoteException e )
                {
                    throw new SpaceException( "Remote problems: " + e.getMessage(), e );
                }
                catch( UnusableEntryException e )
                {
                    e.printStackTrace();
                }
                catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public SpaceTransaction newTransaction()
    {
        synchronized( this )
        {
            if( transactionService.service != null )
            {
                try
                {
                    Transaction.Created created = TransactionFactory.create( transactionService.service, 60000 );
                    Transaction transaction = created.transaction;
                    System.out.println( "NEW: " + transaction );
                    TransactionProxy transactionProxy = new TransactionProxy( transaction, this );
                    transactionStack.get().push( transactionProxy );
                    return transactionProxy;
                }
                catch( LeaseDeniedException e )
                {
                    e.printStackTrace();
                }
                catch( RemoteException e )
                {
                    e.printStackTrace();
                }
            }
            return new NullTransactionProxy();
        }
    }

    public boolean isReady()
    {
        return spaceService.service != null && transactionService.service != null;
    }

    public void activate()
        throws Exception
    {
        String groupConfig = my.configuration().groups().get();
        System.out.println( "GROUPS: " + groupConfig );
        String[] groups = convert( groupConfig );
        spaceService = initializeLookup( groups, JavaSpace05.class );
        transactionService = initializeLookup( groups, TransactionManager.class );
    }

    private String[] convert( String data )
    {
        if( data == null )
        {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer( data, ",", false);
        ArrayList<String> result = new ArrayList<String>();
        while( st.hasMoreTokens() )
        {
            String item = st.nextToken().trim();
            result.add( item );
        }
        String[] retVal = new String[ result.size() ];
        return result.toArray( retVal );
    }

    public void passivate()
        throws Exception
    {
        spaceService.lookupCache.terminate();
        transactionService.lookupCache.terminate();
    }

    static <T> ServiceHolder<T> initializeLookup( String[] groups, Class<T> type )
        throws IOException
    {
        ClassLoader classloader = JavaSpacesClientMixin.class.getClassLoader();
        InputStream in = JavaSpacesClientMixin.class.getResourceAsStream( "jini.config" );
        Reader reader = new InputStreamReader( in );
        String[] options = new String[0];
        DiscoveryManagement dm = null;
        try
        {
            net.jini.config.Configuration config = new ConfigurationFile( reader, options, classloader);
            dm = new LookupDiscoveryManager( groups, null, null, config );
        }
        catch( ConfigurationException e )
        {
            e.printStackTrace();
        }
        ServiceDiscoveryManager sdm = new ServiceDiscoveryManager( dm, null );
        Class[] types = new Class[]{ type };
        ServiceTemplate template = new ServiceTemplate( null, types, null );
        LookupCache lookupCache = sdm.createLookupCache( template, null, null );
        return new ServiceHolder<T>( dm, lookupCache );
    }

    Transaction currentTransaction()
    {
        Stack<TransactionProxy> curStack = transactionStack.get();
        if( curStack.size() == 0 )
        {
            System.err.println( "WARNING: Transaction is null." );
            return null;
        }
        TransactionProxy proxy = curStack.peek();
        if( proxy == null )
        {
            System.err.println( "WARNING: Transaction is null." );
            return null;
        }
        else if( proxy.transaction == null )
        {
            System.err.println( "WARNING: Transaction is null." );
        }
        Transaction transaction = proxy.transaction;
        System.out.println( "LOOKUP: " + transaction );
        return transaction;
    }

    void removeTransaction( TransactionProxy transaction )
    {
        Stack<TransactionProxy> curStack = transactionStack.get();
        curStack.remove( transaction );
    }

    public Iterator<Serializable> iterator()
    {
        ArrayList templates = new ArrayList();
        templates.add( new StorageEntry() );
        try
        {
            Transaction txn = currentTransaction();
            MatchSet matchSet = spaceService.service.contents( templates, txn, 60000, 1000 );
            return new EntryIterator( matchSet );
        }
        catch( TransactionException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch( RemoteException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    static class ServiceHolder<T>
        implements DiscoveryListener
    {
        private DiscoveryManagement dm;
        private LookupCache lookupCache;
        T service;

        public ServiceHolder( DiscoveryManagement dm, LookupCache lookupCache )
        {
            this.dm = dm;
            this.lookupCache = lookupCache;
            this.dm.addDiscoveryListener( this );
            lookupCache.addListener( new ServiceDiscoveryListener()
            {
                public void serviceAdded( ServiceDiscoveryEvent event )
                {
                    System.out.println( "Service Added: " + event );
                    service = (T) event.getPostEventServiceItem().service;
                }

                public void serviceRemoved( ServiceDiscoveryEvent event )
                {
                    System.out.println( "Service Removed: " + event );
                    service = null;
                }

                public void serviceChanged( ServiceDiscoveryEvent event )
                {
                    System.out.println( "Service Changed: " + event );
                }
            } );

        }

        public void dispose()
        {
            dm.removeDiscoveryListener( this );
        }

        public void discovered( DiscoveryEvent e )
        {
            System.out.println( "Discovered: " + e.getGroups() );
        }

        public void discarded( DiscoveryEvent e )
        {
            System.out.println( "Discarded: " + e.getGroups() );
        }
    }

    private static class EntryIterator
        implements Iterator<Serializable>
    {
        private MatchSet matchSet;
        private StorageEntry nextEntry;

        public EntryIterator( MatchSet matchSet )
        {
            this.matchSet = matchSet;
            try
            {
                nextEntry = getNext( matchSet );
            }
            catch( RemoteException e )
            {
                e.printStackTrace();
            }
            catch( UnusableEntryException e )
            {
                e.printStackTrace();
            }
        }

        private StorageEntry getNext( MatchSet matchSet )
            throws RemoteException, UnusableEntryException
        {
            StorageEntry storageEntry = (StorageEntry) matchSet.next();
            return storageEntry;
        }

        public boolean hasNext()
        {
            return nextEntry != null;
        }

        public Serializable next()
        {
            Serializable result = nextEntry.data();
            try
            {
                nextEntry = getNext( matchSet );
            }
            catch( RemoteException e )
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            catch( UnusableEntryException e )
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
