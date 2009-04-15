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

public class JndiEntityStoreMixin
//        implements MapEntityStore, Activatable
{
/* TODO
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

    public EntityState newEntityState( EntityReference reference)
        throws EntityStoreException
    {
        if( isReadOnly )
        {
            throw new ReadOnlyEntityStoreException( "JndiEntityStore is read-only." );
        }

        EntityType entityType = getEntityType( reference.type() );
        try
        {
            Attributes attrs = lookup( reference.identity() );
            if( attrs != null && attrs.size() > 1 )
            {
                throw new EntityAlreadyExistsException(reference);
            }
        }
        catch( NamingException e )
        {
            throw new EntityStoreException( e );
        }
        return new DefaultEntityState(reference, entityType );
    }

    public EntityState getEntityState( EntityReference reference)
        throws EntityStoreException
    {
        EntityType entityType = getEntityType( reference.type() );
        try
        {
            String id = reference.identity();
            Attributes attrs = lookup( id );

            long version = getVersion( attrs );
            long lastModified = getLastModified( attrs );
            Map<QualifiedName, Object> properties = getProperties( attrs, entityType );
//            properties.put( "reference", id);
            Map<QualifiedName, EntityReference> manyAssociations = getAssociations( attrs, entityType );
            Map<QualifiedName, Collection<EntityReference>> manyAssociations = getManyAssociations( attrs, entityType );
            return new DefaultEntityState( version,
                                           lastModified,
                    reference,
                                           EntityStatus.LOADED,
                                           entityType,
                                           properties,
                                           manyAssociations,
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

    private Map<QualifiedName, Object> getProperties( Attributes attrs, EntityType entityType )
        throws NamingException
    {
        Map<QualifiedName, Object> result = new HashMap<QualifiedName, Object>();
        Iterable<PropertyType> props = entityType.properties();
        for( PropertyType property : props )
        {
            QualifiedName qualifiedName = property.qualifiedName();
            String propertyName = qualifiedName.name();
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

    private Map<QualifiedName, EntityReference> getAssociations( Attributes attrs, EntityType entityType )
        throws NamingException
    {
        Map<QualifiedName, EntityReference> result = new HashMap<QualifiedName, EntityReference>();
        Iterable<AssociationType> assocs = entityType.manyAssociations();
        for( AssociationType associationType : assocs )
        {
            QualifiedName qualifiedName = associationType.qualifiedName();
            String associationName = qualifiedName.name();
            Attribute attribute = attrs.get( associationName );
            String identity = (String) attribute.get();
            EntityReference entityReference = new EntityReference( identity, associationType.type() );
            result.put( qualifiedName, entityReference);
        }
        return result;
    }

    private Map<QualifiedName, Collection<EntityReference>> getManyAssociations( Attributes attrs, EntityType entityType )
        throws NamingException
    {
        Map<QualifiedName, Collection<EntityReference>> result = new HashMap<QualifiedName, Collection<EntityReference>>();
        Iterable<ManyAssociationType> assocs = entityType.manyAssociations();
        for( ManyAssociationType associationType : assocs )
        {
            QualifiedName qualifiedName = associationType.qualifiedName();
            String associationName = qualifiedName.name();
            Attribute attribute = attrs.get( associationName );
            String identity = (String) attribute.get();
            EntityReference entityReference = new EntityReference( identity, associationType.type() );
            String assocName = attribute.getID();
            Collection<EntityReference> entry = result.get( assocName );
            if( entry == null )
            {
                entry = new ArrayList<EntityReference>();
                result.put( qualifiedName, entry );
            }
            entry.add(entityReference);
        }
        return result;
    }

    private void putProperties( Attributes attrs, EntityType entityType, Map<String, Object> values )
    {
        Iterable<PropertyType> props = entityType.properties();
        for( PropertyType property : props )
        {
            QualifiedName qualifiedName = property.qualifiedName();
            String propertyName = qualifiedName.name();
            if( !RESTRICTED_PROPERTIES.contains( propertyName ) )
            {
                attrs.put( propertyName, values.get( propertyName ) );
            }
        }
    }

    private void putAssociations( Attributes attrs, EntityType entityType, Map<String, EntityReference> manyAssociations )
    {
//        Iterable<AssociationType> assocs = entityType.manyAssociations();
//        for( AssociationType associationType : assocs )
//        {
//            String stateName = associationType.stateName();
//            int pos = stateName.lastIndexOf( ':' );
//            String associationName = stateName.substring( pos );
//            Attribute attribute = attrs.get( associationName );
//            String identity = (String) attribute.get();
//            EntityReference qualifiedIdentity = new EntityReference( identity, associationType.type() );
//            result.put( attribute.getID(), qualifiedIdentity );
//        }
    }

    private void putManyAssociations( Attributes attrs, EntityType entityType, Map<String, Collection<EntityReference>> manyAssociations )
    {
//        Iterable<ManyAssociationType> assocs = entityType.manyAssociations();
//        for( ManyAssociationType associationType : assocs )
//        {
//            String stateName = associationType.stateName();
//            int pos = stateName.lastIndexOf( ':' );
//            String associationName = stateName.substring( pos );
//            Attribute attribute = attrs.get( associationName );
//            String identity = (String) attribute.get();
//            EntityReference qualifiedIdentity = new EntityReference( identity, associationType.type() );
//            String assocName = attribute.getID();
//            Collection<EntityReference> entry = result.get( assocName );
//            if( entry == null )
//            {
//                entry = new ArrayList<EntityReference>();
//                result.put( assocName, entry );
//            }
//            entry.add( qualifiedIdentity );
//        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> updatedStates, Iterable<EntityReference> removedStates )
        throws EntityStoreException
    {
//        if( isReadOnly )
        {
            throw new ReadOnlyEntityStoreException( "JndiEntityStore is read-only." );
        }
//        boolean turbo = configuration.configuration().turboMode().get();
//        lock.writeLock().lock();
//
//        long lastModified = System.currentTimeMillis();
//        try
//        {
//            storeNewStates( newStates, lastModified );
//            storeLoadedStates( loadedStates, lastModified );
//            removeRemovedStates( removedStates, lastModified );
//        }
//        catch( Throwable e )
//        {
//            lock.writeLock().unlock();
//            if( e instanceof EntityStoreException )
//            {
//                throw (EntityStoreException) e;
//            }
//            else
//            {
//                throw new EntityStoreException( e );
//            }
//        }
//
//        return new StateCommitter()
//        {
//            public void commit()
//            {
//                try
//                {
//                    recordManager.commit();
//                }
//                catch( IOException e )
//                {
//                    e.printStackTrace();
//                }
//                finally
//                {
//                    lock.writeLock().unlock();
//                }
//            }
//
//            public void cancel()
//            {
//
//                try
//                {
//                    recordManager.rollback();
//                    initializeIndex(); // HTree indices are invalid after rollbacks according to the JDBM docs
//                }
//                catch( IOException e )
//                {
//                    e.printStackTrace();
//                }
//                finally
//                {
//                    lock.writeLock().unlock();
//                }
//            }
//        };
    }

    private void storeNewStates( Iterable<EntityState> newStates, long lastModified )
    {
//        for( EntityState newEntity : newStates )
//        {
//            DefaultEntityState defState = (DefaultEntityState) newEntity;
//            Attributes attrs = lookup( defState.qualifiedIdentity().identity() );
//            putProperties( attrs, defState.entityType(), defState.getProperties() );
//            putAssociations( attrs, defState.entityType(), defState.getAssociations() );
//            putManyAssociations( attrs, defState.entityType(), defState.getManyAssociations() );
//        }
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
                        EntityReference reference = new EntityReference( id, qualifiedType );
                        EntityType entityType = getEntityType( reference.type() );

                        long version = Long.parseLong( (String) attributes.get( instanceVersionAttribute ).get() );
                        long lastModified = Long.parseLong( (String) attributes.get( lastModifiedDateAttribute ).get() );
                        Map<QualifiedName, Object> properties = getProperties( attributes, entityType );
                        Map<QualifiedName, EntityReference> manyAssociations = getAssociations( attributes, entityType );
                        Map<QualifiedName, Collection<EntityReference>> manyAssociations = getManyAssociations( attributes, entityType );
                        return new DefaultEntityState( version,
                                                       lastModified,
                                reference,
                                                       EntityStatus.LOADED,
                                                       entityType,
                                                       properties,
                                                       manyAssociations,
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
*/
}