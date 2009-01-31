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
package org.qi4j.entitystore.jndi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.ReadOnlyEntityStoreException;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.property.PropertyType;

public class JndiEntityStoreMixin extends EntityTypeRegistryMixin
    implements Activatable
{
    private static final ArrayList<String> RESTRICTED_PROPERTIES = new ArrayList<String>( );

    static
    {
        RESTRICTED_PROPERTIES.add( "identity" );    
    }


    private @This ReadWriteLock lock;
    @Uses private ServiceDescriptor descriptor;
    @This private Configuration<JndiConfiguration> configuration;
    private InitialDirContext context;
    private String instanceVersionAttribute;
    private String lastModifiedDateAttribute;
    private Boolean isReadOnly;
    private String identityAttribute;
    private String baseDn;
    private String qualifiedTypeAttribute;

    public JndiEntityStoreMixin()
    {
    }

    public void activate()
        throws Exception
    {
        connect();
    }

    private void connect()
        throws NamingException
    {
        JndiConfiguration conf = configuration.configuration();
        instanceVersionAttribute = conf.versionAttribute().get();
        if( instanceVersionAttribute == null )
        {
            instanceVersionAttribute = "instanceVersion";
        }
        lastModifiedDateAttribute = conf.lastModifiedDateAttribute().get();
        if( lastModifiedDateAttribute == null )
        {
            lastModifiedDateAttribute = "lastModifiedDate";
        }
        identityAttribute = conf.identityAttribute().get();
        if( identityAttribute == null )
        {
            identityAttribute = "uid";
        }
        qualifiedTypeAttribute = conf.qualifiedTypeAttribute().get();
        baseDn = conf.baseDN().get();
        isReadOnly = conf.readOnly().get();

        Hashtable<String, String> env = new Hashtable<String, String>();
        addToEnv( env, Context.AUTHORITATIVE, conf.authorative(), null );
        addToEnv( env, Context.BATCHSIZE, conf.batchSize(), null );
        addToEnv( env, Context.DNS_URL, conf.dnsUrl(), null );
        addToEnv( env, Context.INITIAL_CONTEXT_FACTORY, conf.initialContextFactory(), "com.sun.jndi.ldap.LdapCtxFactory" );
        addToEnv( env, Context.LANGUAGE, conf.language(), null );
        addToEnv( env, Context.OBJECT_FACTORIES, conf.objectFactories(), null );
        addToEnv( env, Context.PROVIDER_URL, conf.providerUrl(), null );
        addToEnv( env, Context.REFERRAL, conf.referral(), null );
        addToEnv( env, Context.SECURITY_AUTHENTICATION, conf.securityAuthentication(), null );
        addToEnv( env, Context.SECURITY_CREDENTIALS, conf.securityCredentials(), null );
        addToEnv( env, Context.SECURITY_PRINCIPAL, conf.securityPrincipal(), null );
        addToEnv( env, Context.SECURITY_PROTOCOL, conf.securityProtocol(), null );
        addToEnv( env, Context.STATE_FACTORIES, conf.stateFactories(), null );
        addToEnv( env, Context.URL_PKG_PREFIXES, conf.urlPkgPrefixes(), null );
        context = new InitialDirContext( env );
    }

    private void addToEnv( Hashtable<String, String> env, String key, Property<String> property, String defaultValue )
    {
        String value = property.get();
        if( value != null )
        {
            env.put( key, value );
        }
        else if( defaultValue != null )
        {
            env.put( key, defaultValue );
        }
    }

    public void passivate()
        throws Exception
    {
        context.close();
    }

    public EntityState newEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
//        if( isReadOnly )
        {
            throw new ReadOnlyEntityStoreException( "JndiEntityStore is read-only." );
        }
    }

    public EntityState getEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );
        try
        {
            String id = identity.identity();
            Attributes attrs = lookup( id );

            long version = getVersion( attrs );
            long lastModified = getLastModified( attrs );
            Map<String, Object> properties = getProperties( attrs, entityType );
//            properties.put( "identity", id);
            Map<String, QualifiedIdentity> associations = getAssociations( attrs, entityType );
            Map<String, Collection<QualifiedIdentity>> manyAssociations = getmanyAssociations( attrs, entityType );
            return new DefaultEntityState( version,
                                           lastModified,
                                           identity,
                                           EntityStatus.LOADED,
                                           entityType,
                                           properties,
                                           associations,
                                           manyAssociations );
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
    }

    private long getLastModified( Attributes attrs )
        throws NamingException
    {
        Attribute lastModifiedAttr = attrs.get( lastModifiedDateAttribute );
        if( lastModifiedAttr == null )
        {
            return -1;
        }
        String lastModifiedValue = (String) lastModifiedAttr.get();
        long lastModified = Long.parseLong( lastModifiedValue );
        return lastModified;
    }

    private long getVersion( Attributes attrs )
        throws NamingException
    {
        Attribute versionAttr = attrs.get( instanceVersionAttribute );
        if( versionAttr == null )
        {
            return -1;
        }
        String versionValue = (String) versionAttr.get();
        long version = Long.parseLong( versionValue );
        return version;
    }

    private Attributes lookup( String id )
        throws NamingException
    {
        // TODO: Caching
        LdapName dn = new LdapName( identityAttribute + "=" + id + "," + baseDn );
        Attributes attrs = context.getAttributes( dn );
        return attrs;
    }

    private Map<String, QualifiedIdentity> getAssociations( Attributes attrs, EntityType entityType )
        throws NamingException
    {
        Map<String, QualifiedIdentity> result = new HashMap<String, QualifiedIdentity>();
        Iterable<AssociationType> assocs = entityType.associations();
        for( AssociationType associationType : assocs )
        {
            String qualifiedName = associationType.qualifiedName();
            int pos = qualifiedName.lastIndexOf( ':' );
            String associationName = qualifiedName.substring( pos );
            Attribute attribute = attrs.get( associationName );
            String identity = (String) attribute.get();
            QualifiedIdentity qualifiedIdentity = new QualifiedIdentity( identity, associationType.type() );
            result.put( attribute.getID(), qualifiedIdentity );
        }
        return result;
    }

    private Map<String, Collection<QualifiedIdentity>> getmanyAssociations( Attributes attrs, EntityType entityType )
        throws NamingException
    {
        Map<String, Collection<QualifiedIdentity>> result = new HashMap<String, Collection<QualifiedIdentity>>();
        Iterable<ManyAssociationType> assocs = entityType.manyAssociations();
        for( ManyAssociationType associationType : assocs )
        {
            String qualifiedName = associationType.qualifiedName();
            int pos = qualifiedName.lastIndexOf( ':' );
            String associationName = qualifiedName.substring( pos );
            Attribute attribute = attrs.get( associationName );
            String identity = (String) attribute.get();
            QualifiedIdentity qualifiedIdentity = new QualifiedIdentity( identity, associationType.type() );
            String assocName = attribute.getID();
            Collection<QualifiedIdentity> entry = result.get( assocName );
            if( entry == null )
            {
                entry = new ArrayList<QualifiedIdentity>();
                result.put( assocName, entry );
            }
            entry.add( qualifiedIdentity );
        }
        return result;
    }

    private Map<String, Object> getProperties( Attributes attrs, EntityType entityType )
        throws NamingException
    {
        Map<String, Object> result = new HashMap<String, Object>();
        Iterable<PropertyType> props = entityType.properties();
        for( PropertyType property : props )
        {
            String qualifiedName = property.qualifiedName();
            int pos = qualifiedName.lastIndexOf( ':' );
            String propertyName = qualifiedName.substring( pos + 1 );
            if( !RESTRICTED_PROPERTIES.contains( propertyName ) )
            {
                Attribute attribute = attrs.get( propertyName );
                if( attribute != null )
                {
                    result.put( qualifiedName, attribute.get() );
                }
            }
        }
        return result;
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> updatedStates, Iterable<QualifiedIdentity> removedStates )
        throws EntityStoreException
    {
//        if( isReadOnly )
        {
            throw new ReadOnlyEntityStoreException( "JndiEntityStore is read-only." );
        }
    }

    public Iterator<EntityState> iterator()
    {
        try
        {
            if( qualifiedTypeAttribute == null )
            {
                throw new UnsupportedOperationException( "Without an qualifiedTypeAttribute, this operation is not possible." );
            }
            LdapName name = new LdapName( baseDn );

            final NamingEnumeration<SearchResult> result = context.search( name, "", null );

            return new Iterator<EntityState>()
            {
                public boolean hasNext()
                {
                    try
                    {
                        return result.hasMore();
                    }
                    catch( NamingException e )
                    {
                        return false;
                    }
                }

                public EntityState next()
                {

                    try
                    {
                        Attributes attributes = result.next().getAttributes();
                        String qualifiedType = (String) attributes.get( qualifiedTypeAttribute ).get();
                        String id = (String) attributes.get( identityAttribute ).get();
                        QualifiedIdentity identity = new QualifiedIdentity( id, qualifiedType );
                        EntityType entityType = getEntityType( identity.type() );

                        long version = Long.parseLong( (String) attributes.get( instanceVersionAttribute ).get() );
                        long lastModified = Long.parseLong( (String) attributes.get( lastModifiedDateAttribute ).get() );
                        Map<String, Object> properties = getProperties( attributes, entityType );
                        Map<String, QualifiedIdentity> associations = getAssociations( attributes, entityType );
                        Map<String, Collection<QualifiedIdentity>> manyAssociations = getmanyAssociations( attributes, entityType );
                        return new DefaultEntityState( version,
                                                       lastModified,
                                                       identity,
                                                       EntityStatus.LOADED,
                                                       entityType,
                                                       properties,
                                                       associations,
                                                       manyAssociations );
                    }
                    catch( Exception e )
                    {
                        throw new EntityStoreException( e );
                    }
                }

                public void remove()
                {
                }
            };
        }
        catch( NamingException e )
        {
            return new NullIterator();
        }
    }

    private class NullIterator
        implements Iterator<EntityState>
    {
        public boolean hasNext()
        {
            return false;
        }

        public EntityState next()
        {
            return null;
        }

        public void remove()
        {
        }
    }
}