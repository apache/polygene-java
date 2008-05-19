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

import com.ibatis.sqlmap.client.SqlMapClient;
import static com.ibatis.sqlmap.client.SqlMapClientBuilder.buildSqlMapClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.qi4j.composite.Composite;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.composite.scope.This;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializer;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerConfiguration;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import static org.qi4j.spi.entity.EntityStatus.LOADED;
import static org.qi4j.spi.entity.EntityStatus.NEW;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.structure.Module;

/**
 * TODO: Figure out how does transaction supposed for all EntityStore methods.
 * TODO: identity is a keyword in SQL. We need to have an alias for this identity property for query purposes.
 *
 * @author edward.yakop@gmail.com
 */
final class IBatisEntityStore
    implements EntityStore, Activatable
{
    private final Configuration<IBatisConfiguration> iBatisConfiguration;
    private final Configuration<DBInitializerConfiguration> dbInitializerInfo;

    private SqlMapClient client;

    /**
     * Construct a new instance of {@code IBatisEntityStore}.
     *
     * @param ibatisConfiguration       The entity store service info. This argument must not be {@code null}.
     * @param dbInitializerConfiguration The db initializer info.
     * @since 0.1.0
     */
    public IBatisEntityStore( @This final Configuration<IBatisConfiguration> ibatisConfiguration,
                       @This( optional = true ) final Configuration<DBInitializerConfiguration> dbInitializerConfiguration )
    {
        validateNotNull( "Configuration<IBatisConfiguration>", ibatisConfiguration );
        iBatisConfiguration = ibatisConfiguration;
        dbInitializerInfo = dbInitializerConfiguration;

        client = null;
    }

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

        throwIfNotActive();

        return new IBatisEntityState( aCompositeDescriptor, anIdentity, new HashMap<String, Object>(), 0L, NEW );
    }

    /**
     * Throws {@link EntityStoreException} if this service is not active.
     *
     * @throws EntityStoreException Thrown if this service instance is not active.
     * @since 0.1.0
     */
    private void throwIfNotActive()
        throws EntityStoreException
    {
        if( client == null )
        {
            final String message = "Possibly bug in the qi4j where the store is not activate but its service is invoked.";
            throw new EntityStoreException( message );
        }
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
        throwIfNotActive();

        final Map<String,Object> propertyValues = getRawData( aDescriptor, anIdentity );
        final Long version = (Long) propertyValues.get( "VERSION" );
        return new IBatisEntityState( aDescriptor, anIdentity, propertyValues, version, LOADED );
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
    private Map<String,Object> getRawData( final CompositeDescriptor aDescriptor, final QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aDescriptor );
        try
        {
            final String identityAsString = anIdentity.getIdentity();
            final String statementId = getStatementId( aDescriptor, "getById" );
            final Map<String,Object> compositePropertyValues =
                (Map<String,Object>) client.queryForObject( statementId, identityAsString );
            if( compositePropertyValues == null )
            {
                throw new EntityNotFoundException( this.toString(), identityAsString );
            }

            return compositePropertyValues;
        }
        catch( SQLException e )
        {
            throw new EntityStoreException( e );
        }

    }

    private String getStatementId( final CompositeDescriptor aDescriptor, final String suffix )
    {
        return getNameSpace( aDescriptor ) + "." + suffix;
    }

    private String getNameSpace( final CompositeDescriptor aDescriptor )
    {
        final CompositeModel compositeModel = aDescriptor.getCompositeModel();
        final Class<? extends Composite> compositeClass = compositeModel.getCompositeType();
        return compositeClass.getName();
    }


    public final StateCommitter prepare(
        final Iterable<EntityState> newStates,
        final Iterable<EntityState> loadedStates,
        final Iterable<QualifiedIdentity> removedStates,
        final Module aModule )
        throws EntityStoreException
    {
        // TODO
        return null;
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
        throws IOException, SQLException
    {
        initializeDatabase();

        final IBatisConfiguration configuration = getUpdatedConfiguration();

        client = newSqlMapClient( configuration );
    }

    private SqlMapClient newSqlMapClient( final IBatisConfiguration configuration )
        throws IOException
    {
        // Initialize client
        final String configURL = configuration.sqlMapConfigURL().get();
        final InputStream configInputStream = new URL( configURL ).openStream();

        final Properties properties = configuration.configProperties().get();
        return buildSqlMapClient( configInputStream, properties );
    }

    private IBatisConfiguration getUpdatedConfiguration()
    {
        iBatisConfiguration.refresh();
        return iBatisConfiguration.configuration();
    }

    private void initializeDatabase()
        throws SQLException, IOException
    {
        // Initialize database if required.
        if( dbInitializerInfo != null )
        {
            dbInitializerInfo.refresh();

            final DBInitializerConfiguration configuration = dbInitializerInfo.configuration();
            final DBInitializer dbInitializer = new DBInitializer( configuration );
            dbInitializer.initialize();
        }
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
        // clean up client
        client = null;
    }

}
