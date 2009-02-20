/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.entitystore.legacy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.entitystore.legacy.dbInitializer.DBInitializer;
import org.qi4j.entitystore.legacy.internal.LegacyEntityState;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import static org.qi4j.spi.entity.EntityStatus.LOADED;
import static org.qi4j.spi.entity.EntityStatus.NEW;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UnknownEntityTypeException;

/**
 * JAVADOC: Figure out how does transaction supposed for all EntityStore methods.
 * JAVADOC: identity is a keyword in SQL. We need to have an alias for this identity property for query purposes.
 */
public class LegacySqlEntityStore
    implements EntityStore, Activatable
{
    private static final String VERSION = "VERSION";
    private static final String LASTMODIFIED = "LASTMODIFIED";


    @Structure private Qi4jSPI spi;
    @Structure private Module module;

    @This private Configuration<LegacySqlConfiguration> iBatisConfiguration;
    private LegacySqlClient config;

    private Map<String, EntityType> entityTypes = new HashMap<String, EntityType>();

    public void registerEntityType( EntityType entityType )
    {
        entityTypes.put( entityType.type(), entityType );
    }

    public EntityType getEntityType( String aEntityType )
    {
        return entityTypes.get( aEntityType );
    }

    /**
     * Construct a new instance of entity state.
     *
     * @param identity The identity. This argument must not be {@code null}.
     * @return The new entity state given the arguments.
     * @throws EntityStoreException Thrown if this service is not active.
     * @since 0.2.0
     */
    public final EntityState newEntityState( final QualifiedIdentity identity )
        throws EntityStoreException
    {
        validateNotNull( "anIdentity", identity );

        checkActivation();

        EntityType type = getEntityType( identity );
        return new LegacyEntityState( type, identity, new HashMap<String, Object>(), 0L, System.currentTimeMillis(), NEW );
    }

    /**
     * Throws {@link EntityStoreException} if this service is not active.
     *
     * @throws EntityStoreException Thrown if this service instance is not active.
     * @since 0.1.0
     */
    private void checkActivation()
        throws EntityStoreException
    {
        if( config == null )
        {
            throw new EntityStoreException( "LegacySqlEntityStore not activated." );
        }
        config.checkActive();
    }

    /**
     * Get the entity state given the composite descriptor and identity.
     *
     * @param anIdentity The entity identity. This argument must not be {@code null}.
     * @return The entity state given the descriptor and identity.
     * @throws EntityStoreException    Thrown if retrieval failed.
     * @throws EntityNotFoundException Thrown if the entity does not exists.
     * @since 0.2.0
     */
    public final EntityState
    getEntityState( final QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        checkActivation();
        final Map<String, Object> rawData = getRawData( anIdentity );
        Long version = (Long) rawData.get( VERSION );
        if( version == null )
        {
            version = new Long( 0 );
        }

        Long lastModified = (Long) rawData.get( LASTMODIFIED );
        if( lastModified == null )
        {
            lastModified = System.currentTimeMillis();
        }

        return new LegacyEntityState( getEntityType( anIdentity ), anIdentity, rawData, version, lastModified, LOADED );
    }


    /**
     * Returns raw data given the composite class.
     *
     * @param anIdentity The identity. This argument must not be {@code null}.
     * @return The raw data given input.
     * @throws EntityStoreException Thrown if retrieval failed.
     * @since 0.1.0
     */
    private Map<String, Object> getRawData( final QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        validateNotNull( "anIdentity", anIdentity );
        checkActivation();
        final Map<String, Object> compositePropertyValues = config.executeLoad( anIdentity );
        if( compositePropertyValues == null )
        {
            throw new EntityNotFoundException( this.toString(), anIdentity );
        }

        return compositePropertyValues;
    }

    private EntityType getEntityType( QualifiedIdentity identity )
        throws UnknownEntityTypeException
    {
        EntityType type = entityTypes.get( identity.type() );
        if( type == null )
        {
            throw new UnknownEntityTypeException( identity.type() );
        }
        return type;
    }


    public final StateCommitter prepare(
        final Iterable<EntityState> newStates,
        final Iterable<EntityState> loadedStates,
        final Iterable<QualifiedIdentity> removedStates )
        throws EntityStoreException
    {
        checkActivation();

        config.startTransaction();

        for( final EntityState state : newStates )
        {
            Map<String, Object> properties = getProperties( state );
            properties.put( VERSION, 1 );
            properties.put( LASTMODIFIED, System.currentTimeMillis() );
            config.executeUpdate( "insert", state.qualifiedIdentity(), properties );
        }
        for( final EntityState state : loadedStates )
        {
            Map<String, Object> properties = getProperties( state );
            properties.put( VERSION, state.version() + 1 );
            properties.put( LASTMODIFIED, System.currentTimeMillis() );
            config.executeUpdate( "update", state.qualifiedIdentity(), properties );
        }
        for( final QualifiedIdentity identity : removedStates )
        {
            config.executeUpdate( "delete", identity, identity.identity() );
        }

        return config;
    }


    private Map<String, Object> getProperties( final EntityState state )
    {
        final Map<String, Object> result = new HashMap<String, Object>();
        for( final String propertyName : state.propertyNames() )
        {
            result.put( propertyName, state.getProperty( propertyName ) );
        }
        for( final String assocName : state.associationNames() )
        {
            result.put( assocName, state.getAssociation( assocName ).identity() );
        }
        for( final String manyAssocName : state.manyAssociationNames() )
        {
            final Collection<QualifiedIdentity> manyAssociation = state.getManyAssociation( manyAssocName );
            result.put( manyAssocName, stringIdentifiersOf( manyAssociation ) );
        }
        return result;
    }

    private Collection<String> stringIdentifiersOf( final Collection<QualifiedIdentity> qualifiedIdentities )
    {
        final Collection<String> identifiers = new ArrayList<String>( qualifiedIdentities.size() );
        for( final QualifiedIdentity identity : qualifiedIdentities )
        {
            identifiers.add( identity.identity() );
        }
        return identifiers;
    }

    /**
     * Not supported.
     *
     * @return {@code null}.
     */
    public final Iterator<EntityState> iterator()
    {
        return null;
    }

    /**
     * Activate this service.
     *
     * @throws IOException  If reading sql map configuration failed.
     * @throws SQLException Thrown if database initialization failed.
     * @since 0.1.0
     */
    public final void activate()
        throws Exception
    {
        iBatisConfiguration.refresh();
        initializeDatabase();
        LegacySqlConfiguration configuration = iBatisConfiguration.configuration();
        config = new LegacySqlClient( configuration.sqlMapConfigURL().get(), configuration.configProperties().get() );
        config.activate();
    }

    private void initializeDatabase()
        throws SQLException, IOException
    {
        final DBInitializer dbInitializer = new DBInitializer();
        LegacySqlConfiguration configuration = iBatisConfiguration.configuration();
        Properties connectionProperties = configuration.connectionProperties().get();
        String schemaUrl = configuration.schemaUrl().get();
        String dataUrl = configuration.dataUrl().get();
        String dbUrl = configuration.dbUrl().get();
        dbInitializer.initialize(schemaUrl, dataUrl, dbUrl, connectionProperties);
    }

    /**
     * Passivate this service.
     *
     * @throws Exception Thrown if there is any passivation problem.
     * @since 0.1.0
     */
    public final void passivate()
        throws Exception
    {
        if( config != null )
        {
            config.passivate();
        }
    }
}
