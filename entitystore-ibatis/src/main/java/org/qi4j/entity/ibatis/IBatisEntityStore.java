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
package org.qi4j.entity.ibatis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializer;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerConfiguration;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.injection.scope.This;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import static org.qi4j.spi.entity.EntityStatus.LOADED;
import static org.qi4j.spi.entity.EntityStatus.NEW;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.structure.Module;

/**
 * TODO: Figure out how does transaction supposed for all EntityStore methods.
 * TODO: identity is a keyword in SQL. We need to have an alias for this identity property for query purposes.
 */
public class IBatisEntityStore
    implements EntityStore, Activatable
{
    @This private Configuration<IBatisConfiguration> iBatisConfiguration;
    private IbatisClient config;

    /**
     * Construct a new instance of entity state.
     *
     * @param aCompositeDescriptor The composite descriptor. This argument must not be {@code null}.
     * @param anIdentity           The identity. This argument must not be {@code null}.
     * @return The new entity state given the arguments.
     * @throws EntityStoreException Thrown if this service is not active.
     * @since 0.2.0
     */
    public final EntityState newEntityState( final CompositeDescriptor aCompositeDescriptor, final QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        validateNotNull( "aCompositeDescriptor", aCompositeDescriptor );
        validateNotNull( "anIdentity", anIdentity );

        checkActivation();

        return new IBatisEntityState( aCompositeDescriptor, anIdentity, new HashMap<String, Object>(), 0L, NEW );
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
            throw new EntityStoreException( "IBatisEntityStore not activated." );
        }
        config.checkActive();
    }

    /**
     * Get the entity state given the composite descriptor and identity.
     *
     * @param aDescriptor The entity composite descriptor. This argument must not be {@code null}.
     * @param anIdentity  The entity identity. This argument must not be {@code null}.
     * @return The entity state given the descriptor and identity.
     * @throws EntityStoreException    Thrown if retrieval failed.
     * @throws EntityNotFoundException Thrown if the entity does not exists.
     * @since 0.2.0
     */
    public final EntityState getEntityState( final CompositeDescriptor aDescriptor, final QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        checkActivation();
        final Map<String, Object> rawData = getRawData( aDescriptor, anIdentity );
        final Long version = (Long) rawData.get( "VERSION" );
        return new IBatisEntityState( aDescriptor, anIdentity, rawData, version, LOADED );
    }


    /**
     * Returns raw data given the composite class.
     *
     * @param aDescriptor The descriptor. This argument must not be {@code null}.
     * @param anIdentity  The identity. This argument must not be {@code null}.
     * @return The raw data given input.
     * @throws EntityStoreException Thrown if retrieval failed.
     * @since 0.1.0
     */
    private Map<String, Object> getRawData( final CompositeDescriptor aDescriptor, final QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aDescriptor );
        checkActivation();
        final Map<String, Object> compositePropertyValues = config.executeLoad( anIdentity );
        if( compositePropertyValues == null )
        {
            throw new EntityNotFoundException( this.toString(), anIdentity.identity() );
        }

        return compositePropertyValues;
    }


    public final StateCommitter prepare(
        final Iterable<EntityState> newStates,
        final Iterable<EntityState> loadedStates,
        final Iterable<QualifiedIdentity> removedStates,
        final Module module )
        throws EntityStoreException
    {
        checkActivation();

        config.startTransaction();

        for( final EntityState state : newStates )
        {
            config.executeUpdate( "insert", state.getIdentity(), getProperties( state ) );
        }
        for( final EntityState state : loadedStates )
        {
            config.executeUpdate( "update", state.getIdentity(), getProperties( state ) );
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
        for( final String propertyName : state.getPropertyNames() )
        {
            result.put( propertyName, state.getProperty( propertyName ) );
        }
        for( final String assocName : state.getAssociationNames() )
        {
            result.put( assocName, state.getAssociation( assocName ).identity() );
        }
        for( final String manyAssocName : state.getManyAssociationNames() )
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
        final IBatisConfiguration configuration = iBatisConfiguration.configuration();

        initializeDatabase( configuration );

        config = new IbatisClient( configuration.sqlMapConfigURL().get(), configuration.configProperties().get() );
        config.activate();
    }

    private void initializeDatabase( final DBInitializerConfiguration configuration )
        throws SQLException, IOException
    {
        final DBInitializer dbInitializer = new DBInitializer( configuration );
        dbInitializer.initialize();
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
