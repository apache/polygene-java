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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
    private final Configuration<IBatisEntityStoreServiceInfo> serviceInfo;
    private final Configuration<DBInitializerInfo> dbInitializerInfo;

    private SqlMapClient client;

    /**
     * Construct a new instance of {@code IBatisEntityStore}.
     *
     * @param aServiceInfo       The entity store service info. This argument must not be {@code null}.
     * @param aDBInitializerInfo The db initializer info.
     * @since 0.1.0
     */
    IBatisEntityStore( @This Configuration<IBatisEntityStoreServiceInfo> aServiceInfo,
                       @This( optional = true )Configuration<DBInitializerInfo> aDBInitializerInfo )
    {
        serviceInfo = aServiceInfo;
        dbInitializerInfo = aDBInitializerInfo;

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
    public final EntityState newEntityState( CompositeDescriptor aCompositeDescriptor, QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        validateNotNull( "aCompositeDescriptor", aCompositeDescriptor );
        validateNotNull( "anIdentity", anIdentity );

        throwIfNotActive();

        return new IBatisEntityState( aCompositeDescriptor, anIdentity, new HashMap<String, Object>(), 0, NEW );
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
            String message = "Possibly bug in the qi4j where the store is not activate but its service is invoked.";
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
    @SuppressWarnings( "unchecked" )
    public final EntityState getEntityState( CompositeDescriptor aDescriptor, QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        throwIfNotActive();

        Map propertyValues = getRawData( aDescriptor, anIdentity );
        Integer version = (Integer) propertyValues.get( "VERSION" );
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
    private Map getRawData( CompositeDescriptor aDescriptor, QualifiedIdentity anIdentity )
        throws EntityStoreException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aDescriptor );
        CompositeModel compositeModel = aDescriptor.getCompositeModel();
        Class<? extends Composite> compositeClass = compositeModel.getCompositeType();
        String statementId = compositeClass.getName() + ".getById";

        String identityAsString = anIdentity.getIdentity();
        Map compositePropertyValues;
        try
        {
            compositePropertyValues = (Map) client.queryForObject( statementId, identityAsString );
        }
        catch( SQLException e )
        {
            throw new EntityStoreException( e );
        }

        if( compositePropertyValues == null )
        {
            throw new EntityNotFoundException( this.toString(), identityAsString );
        }

        return compositePropertyValues;
    }


    public final StateCommitter prepare(
        Iterable<EntityState> newStates,
        Iterable<EntityState> loadedStates,
        Iterable<QualifiedIdentity> removedStates,
        Module aModule )
        throws EntityStoreException
    {
        // TODO
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        // Initialize database if required.
        if( dbInitializerInfo != null )
        {
            dbInitializerInfo.refresh();

            DBInitializerInfo configuration = dbInitializerInfo.configuration();
            DBInitializer dbInitializer = new DBInitializer( configuration );
            dbInitializer.initialize();
        }

        serviceInfo.refresh();

        IBatisEntityStoreServiceInfo configuration = serviceInfo.configuration();

        // Initialize client
        String configURL = configuration.getSQLMapConfigURL();
        InputStream configStream = new URL( configURL ).openStream();
        InputStreamReader streamReader = new InputStreamReader( configStream );
        Reader bufferedReader = new BufferedReader( streamReader );

        Properties properties = configuration.getConfigProperties();
        if( properties == null )
        {
            client = buildSqlMapClient( bufferedReader );
        }
        else
        {
            client = buildSqlMapClient( bufferedReader, properties );
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
