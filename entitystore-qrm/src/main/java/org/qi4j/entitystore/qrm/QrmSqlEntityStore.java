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
package org.qi4j.entitystore.qrm;

import org.qi4j.api.io.Input;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.structure.ModuleSPI;

/**
 * JAVADOC: Figure out how does transaction supposed to work for all EntityStore methods.
 * JAVADOC: identity is a keyword in SQL. We need to have an alias for this identity property for query purposes.
 */
public class QrmSqlEntityStore
    implements EntityStore, Activatable
{
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, ModuleSPI module, long currentTime )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Input<EntityState, EntityStoreException> entityStates( ModuleSPI module )
    {
        return null;
    }

    public void activate()
        throws Exception
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void passivate()
        throws Exception
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
//    private static final QualifiedName VERSION = QualifiedName.fromQN( "VERSION" );
//    private static final QualifiedName LASTMODIFIED = QualifiedName.fromQN( "LASTMODIFIED" );
//
//
//    @Structure private Qi4jSPI spi;
//    @Structure private Module module;
//
//    @This private Configuration<QrmSqlConfiguration> iBatisConfiguration;
//    private QrmSqlClient config;
//
//    /**
//     * Construct a new instance of entity state.
//     *
//     * @param identity The identity. This argument must not be {@code null}.
//     * @return The new entity state given the arguments.
//     * @throws EntityStoreException Thrown if this service is not active.
//     */
//    public final EntityState newEntityState( final QualifiedIdentity identity )
//        throws EntityStoreException
//    {
//        validateNotNull( "anIdentity", identity );
//
//        checkActivation();
//
//        EntityType type = getEntityType( identity );
//        return null; // new QrmEntityState( type, identity, new HashMap<QualifiedName, Object>(), 0L, System.currentTimeMillis(), NEW );
//    }
//
//    /**
//     * Throws {@link EntityStoreException} if this service is not active.
//     *
//     * @throws EntityStoreException Thrown if this service instance is not active.
//     */
//    private void checkActivation()
//        throws EntityStoreException
//    {
//        if( config == null )
//        {
//            throw new EntityStoreException( "QrmSqlEntityStore not activated." );
//        }
//        config.checkActive();
//    }
//
//    /**
//     * Get the entity state given the composite descriptor and identity.
//     *
//     * @param anIdentity The entity identity. This argument must not be {@code null}.
//     * @return The entity state given the descriptor and identity.
//     * @throws EntityStoreException    Thrown if retrieval failed.
//     * @throws EntityNotFoundException Thrown if the entity does not exists.
//     */
//    public final EntityState getEntityState( final QualifiedIdentity anIdentity )
//        throws EntityStoreException, EntityNotFoundException
//    {
//        checkActivation();
//        final Map<QualifiedName, Object> rawData = getRawData( anIdentity );
//        Long version = (Long) rawData.get( VERSION );
//        if( version == null )
//        {
//            version = new Long( 0 );
//        }
//
//        Long lastModified = (Long) rawData.get( LASTMODIFIED );
//        if( lastModified == null )
//        {
//            lastModified = System.currentTimeMillis();
//        }
//
//        return null; //new QrmEntityState( getEntityType( anIdentity ), anIdentity, rawData, version, lastModified, LOADED );
//    }
//
//
//    /**
//     * Returns raw data given the composite class.
//     *
//     * @param anIdentity The identity. This argument must not be {@code null}.
//     * @return The raw data given input.
//     * @throws EntityStoreException Thrown if retrieval failed.
//     */
//    private Map<QualifiedName, Object> getRawData( final QualifiedIdentity anIdentity )
//        throws EntityStoreException
//    {
//        validateNotNull( "anIdentity", anIdentity );
//        checkActivation();
//        final Map<String, Object> rawData = config.executeLoad( anIdentity );
//        if( rawData == null )
//        {
//            throw new EntityNotFoundException( null );
//        }
//
//        final Map<QualifiedName, Object> compositePropertyValues = new HashMap<QualifiedName, Object>();
//        for( Map.Entry<String, Object> stringObjectEntry : rawData.entrySet() )
//        {
//            compositePropertyValues.put( QualifiedName.fromQN( stringObjectEntry.getKey() ), stringObjectEntry.getValue() );
//        }
//
//        return compositePropertyValues;
//    }
//
//    private EntityType getEntityType( QualifiedIdentity identity )
//        throws UnknownEntityTypeException
//    {
//        EntityType type = null; // TODO entityTypes.get( identity.type() );
//        if( type == null )
//        {
//            throw new UnknownEntityTypeException( identity.type() );
//        }
//        return type;
//    }
//
//
//    public final StateCommitter prepare(
//        final Iterable<EntityState> newStates,
//        final Iterable<EntityState> loadedStates,
//        final Iterable<QualifiedIdentity> removedStates )
//        throws EntityStoreException
//    {
//        checkActivation();
//
//        config.startTransaction();
//
//        for( final EntityState state : newStates )
//        {
//            Map<QualifiedName, Object> properties = getProperties( state );
//            properties.put( VERSION, 1 );
//            properties.put( LASTMODIFIED, System.currentTimeMillis() );
//            //   config.executeUpdate( "insert", state.identity(), properties );
//        }
//        for( final EntityState state : loadedStates )
//        {
//            Map<QualifiedName, Object> properties = getProperties( state );
//            properties.put( VERSION, state.version() + 1 );
//            properties.put( LASTMODIFIED, System.currentTimeMillis() );
//            //   config.executeUpdate( "update", state.identity(), properties );
//        }
//        for( final QualifiedIdentity identity : removedStates )
//        {
//            config.executeUpdate( "delete", identity, identity.identity() );
//        }
//
//        return config;
//    }
//
//
//    private Map<QualifiedName, Object> getProperties( final EntityState state )
//    {
//        final Map<QualifiedName, Object> result = new HashMap<QualifiedName, Object>();
///*
//        for( final QualifiedName propertyName : state.propertyNames() )
//        {
//            result.put( propertyName, state.getProperty( propertyName ) );
//        }
//        for( final QualifiedName assocName : state.associationNames() )
//        {
//            result.put( assocName, state.getAssociation( assocName ).identity() );
//        }
//        for( final QualifiedName manyAssocName : state.manyAssociationNames() )
//        {
//            final Collection<QualifiedIdentity> manyAssociation = state.getManyAssociation( manyAssocName );
//            result.put( manyAssocName, stringIdentifiersOf( manyAssociation ) );
//        }
//*/
//        return result;
//    }
//
//    private Collection<String> stringIdentifiersOf( final Collection<QualifiedIdentity> qualifiedIdentities )
//    {
//        final Collection<String> identifiers = new ArrayList<String>( qualifiedIdentities.size() );
//        for( final QualifiedIdentity identity : qualifiedIdentities )
//        {
//            identifiers.add( identity.identity() );
//        }
//        return identifiers;
//    }
//
//    /**
//     * Not supported.
//     *
//     * @return {@code null}.
//     */
//    public final Iterator<EntityState> iterator()
//    {
//        return null;
//    }
//
//    /**
//     * Activate this service.
//     *
//     * @throws IOException  If reading sql map configuration failed.
//     * @throws SQLException Thrown if database initialization failed.
//     */
//    public final void activate()
//        throws Exception
//    {
//        iBatisConfiguration.refresh();
//        initializeDatabase();
//        QrmSqlConfiguration configuration = iBatisConfiguration.configuration();
//        config = new QrmSqlClient( configuration.sqlMapConfigURL().get(), configuration.configProperties().get() );
//        config.activate();
//    }
//
//    private void initializeDatabase()
//        throws SQLException, IOException
//    {
//        final DBInitializer dbInitializer = new DBInitializer();
//        QrmSqlConfiguration configuration = iBatisConfiguration.configuration();
//        Properties connectionProperties = configuration.connectionProperties().get();
//        String schemaUrl = configuration.schemaUrl().get();
//        String dataUrl = configuration.dataUrl().get();
//        String dbUrl = configuration.dbUrl().get();
//        dbInitializer.initialize( schemaUrl, dataUrl, dbUrl, connectionProperties );
//    }
//
//    /**
//     * Passivate this service.
//     *
//     * @throws Exception Thrown if there is any passivation problem.
//     */
//    public final void passivate()
//        throws Exception
//    {
//        if( config != null )
//        {
//            config.passivate();
//        }
//    }
//}
