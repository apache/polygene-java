/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.library.ldap.server;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.configuration.Configuration;
import java.util.HashSet;
import java.util.Set;

public class ApacheDirectoryServiceMixin
    implements Activatable
{
    @This Configuration<LdapConfiguration> configuration;

    /** The directory service */
    private DirectoryService service;

    public void activate()
        throws Exception
    {
        try
        {
            String[] attrs = { "objectClass", "ou", "uid"  };
            // Initialize the LDAP service
            service = new DefaultDirectoryService();

            // Disable the ChangeLog system
            service.getChangeLog().setEnabled( false );


            // Create a new partition named 'apache'.
            LdapConfiguration conf = configuration.configuration();
            String partitionId = conf.partitionId().get();
            if( partitionId == null )
            {
                partitionId = "qi4j";
            }
            Partition apachePartition = addPartition( partitionId, conf.partitionDn().get() );

            // Index some attributes on the apache partition
            addIndex( apachePartition, attrs );

            // And start the service
            service.startup();

            // Inject the apache root entry if it does not already exist
            try
        {
            service.getAdminSession().lookup( apachePartition.getSuffixDn() );
            }
            catch ( LdapNameNotFoundException lnnfe )
            {
                LdapDN dnApache = new LdapDN( "dc=Apache,dc=Org" );
                ServerEntry entryApache = service.newEntry( dnApache );
                entryApache.add( "objectClass", "top", "domain", "extensibleObject" );
                entryApache.add( "dc", "Apache" );
                service.getAdminSession().add( entryApache );
            }
        }
        catch( Exception e )
        {
            throw e;
        }
        service.startup();
    }

    public void passivate()
        throws Exception
    {
        service.shutdown();
    }


    /**
     * Add a new partition to the server
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition( String partitionId, String partitionDn ) throws Exception
    {
        // Create a new partition named 'foo'.
        Partition partition = new JdbmPartition();
        partition.setId( partitionId );
        partition.setSuffix( partitionDn );
        service.addPartition( partition );

        return partition;
    }

    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs The list of attributes to index
     */
    private void addIndex( Partition partition, String... attrs )
    {
        // Index some attributes on the apache partition
        Set<Index<?, ServerEntry>> indexedAttributes = new HashSet<Index<?, ServerEntry>>();

        for ( String attribute:attrs )
        {
            indexedAttributes.add( new JdbmIndex<String,ServerEntry>( attribute ) );
        }

//        ((JdbmPartition)partition).setIndexedAttributes( indexedAttributes );
    }
}
