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
package org.qi4j.library.jini.javaspaces;

import java.io.IOException;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Collection;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.security.policy.DynamicPolicyProvider;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryEvent;
import net.jini.space.JavaSpace05;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.injection.scope.Service;
import org.qi4j.library.http.JettyServiceAssembler;
import org.qi4j.library.jini.lookup.JiniLookupServiceAssembler;
import org.qi4j.library.jini.transaction.JiniTransactionServiceAssembler;
import org.qi4j.test.AbstractQi4jTest;

public class OutriggerStartTest extends AbstractQi4jTest
{
    private static Logger logger = Logger.getLogger( "" );

    static
    {
        Handler[] handlers = logger.getHandlers();
        for( Handler handler : handlers )
        {
            Formatter formatter = new SimpleFormatter();
            handler.setFormatter( formatter );
        }
        logger.setLevel( Level.FINEST );

        Policy basePolicy = new AllPolicy();
        DynamicPolicyProvider policyProvider = new DynamicPolicyProvider( basePolicy );
        Policy.setPolicy( policyProvider );
    }


    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addAssembler( new JiniTransactionServiceAssembler() );
        module.addAssembler( new JiniLookupServiceAssembler() );
        module.addAssembler( new JiniJavaSpacesServiceAssembler() );
        module.addAssembler( new JettyServiceAssembler() );
        module.addObjects( Holder.class );
        module.addServices( MemoryEntityStoreService.class );
    }

    @Test
    public void whenStartingOutriggerExpectFoundJavaSpace05()
        throws Exception
    {
        LookupCache cache = initialize();
        MyServiceDiscoveryListener listener = new MyServiceDiscoveryListener();
        cache.addListener( listener );
        Holder object = objectBuilderFactory.newObject( Holder.class );
        synchronized( this )
        {
            if( !listener.added )
            {
                wait( 5000 );
            }
        }
        org.junit.Assert.assertTrue( listener.added );
    }


    private LookupCache initialize()
        throws IOException
    {
        DiscoveryManagement discoveryManager = new LookupDiscoveryManager( null, null, new MyDiscoveryListener() );
        ServiceDiscoveryManager sdm = new ServiceDiscoveryManager( discoveryManager, null );
        Class[] types = new Class[]{ JavaSpace05.class };
        ServiceTemplate template = new ServiceTemplate( null, types, null );
        LookupCache lookupCache = sdm.createLookupCache( template, null, null );
        return lookupCache;

    }

    public static class Holder
    {
        @Service JavaSpacesService service;
    }

    private static class MyDiscoveryListener
        implements DiscoveryListener
    {

        public void discovered( DiscoveryEvent e )
        {
            printEvent( e, "Discovered: " );
        }

        public void discarded( DiscoveryEvent e )
        {
            printEvent( e, "Discarded: " );
        }

        private void printEvent( DiscoveryEvent e, String message )
        {
            Collection<String[]> collection = e.getGroups().values();
            for( String[] array : collection )
            {
                StringBuffer groups = new StringBuffer();
                boolean first = true;
                for( String group : array )
                {
                    if( !first )
                    {
                        groups.append( "," );
                    }
                    first = false;
                    groups.append( group );
                    System.out.println( message + groups );
                }
            }
        }
    }

    private class MyServiceDiscoveryListener
        implements ServiceDiscoveryListener
    {
        boolean added = false;
        boolean removed = false;

        public MyServiceDiscoveryListener()
        {
        }

        public void serviceAdded( ServiceDiscoveryEvent event )
        {
            synchronized( OutriggerStartTest.this )
            {
                logger.info( "Added: " + event.getPostEventServiceItem() );
                added = true;
                OutriggerStartTest.this.notifyAll();
            }
        }

        public void serviceRemoved( ServiceDiscoveryEvent event )
        {
            synchronized( OutriggerStartTest.this )
            {
                logger.info( "Removed: " + event.getPostEventServiceItem() );
                removed = true;
                OutriggerStartTest.this.notifyAll();
            }
        }

        public void serviceChanged( ServiceDiscoveryEvent event )
        {
        }
    }

    public static class AllPolicy extends Policy
    {

        public AllPolicy()
        {
        }

        public PermissionCollection getPermissions( CodeSource codeSource )
        {
            Permissions allPermission;
            allPermission = new Permissions();
            allPermission.add( new AllPermission() );
            return allPermission;
        }

        public void refresh()
        {
        }
    }
}