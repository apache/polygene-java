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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.entitystore.legacy.dbInitializer.DBInitializer;
import org.qi4j.entitystore.legacy.internal.LegacyEntityState;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.*;
import static org.qi4j.spi.entity.EntityStatus.LOADED;
import static org.qi4j.spi.entity.EntityStatus.NEW;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * JAVADOC: Figure out how does transaction supposed for all EntityStore methods.
 * JAVADOC: identity is a keyword in SQL. We need to have an alias for this identity property for query purposes.
 */
public class LegacySqlEntityStore
        implements EntityStore, Activatable
{
    private static final QualifiedName VERSION = QualifiedName.fromQN("VERSION");
    private static final QualifiedName LASTMODIFIED = QualifiedName.fromQN("LASTMODIFIED");


    @Structure
    private Qi4jSPI spi;
    @Structure
    private Module module;

    @This
    private Configuration<LegacySqlConfiguration> iBatisConfiguration;
    private LegacySqlClient config;

    /**
     * Construct a new instance of entity state.
     *
     * @param reference The identity. This argument must not be {@code null}.
     * @return The new entity state given the arguments.
     * @throws EntityStoreException Thrown if this service is not active.
     * @since 0.2.0
     */
    public final EntityState newEntityState(final EntityReference reference)
            throws EntityStoreException
    {
        validateNotNull("anIdentity", reference);

        checkActivation();

        return new LegacyEntityState(null, reference, new HashMap<QualifiedName, Object>(), 0L, System.currentTimeMillis(), NEW);
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
        if (config == null)
        {
            throw new EntityStoreException("LegacySqlEntityStore not activated.");
        }
        config.checkActive();
    }

    /**
     * Get the entity state given the composite descriptor and identity.
     *
     * @param anReference The entity identity. This argument must not be {@code null}.
     * @return The entity state given the descriptor and identity.
     * @throws EntityStoreException    Thrown if retrieval failed.
     * @throws EntityNotFoundException Thrown if the entity does not exists.
     * @since 0.2.0
     */
    public final EntityState getEntityState(final EntityReference anReference)
            throws EntityStoreException
    {
        checkActivation();
        final Map<QualifiedName, Object> rawData = getRawData(anReference);
        Long version = (Long) rawData.get(VERSION);
        if (version == null)
        {
            version = new Long(0);
        }

        Long lastModified = (Long) rawData.get(LASTMODIFIED);
        if (lastModified == null)
        {
            lastModified = System.currentTimeMillis();
        }

        return new LegacyEntityState(null, anReference, rawData, version, lastModified, LOADED);
    }


    /**
     * Returns raw data given the composite class.
     *
     * @param anReference The identity. This argument must not be {@code null}.
     * @return The raw data given input.
     * @throws EntityStoreException Thrown if retrieval failed.
     * @since 0.1.0
     */
    private Map<QualifiedName, Object> getRawData(final EntityReference anReference)
            throws EntityStoreException
    {
        validateNotNull("anReference", anReference);
        checkActivation();
        final Map<String, Object> rawData = config.executeLoad(anReference);
        if (rawData == null)
        {
            throw new EntityNotFoundException(anReference);
        }

        final Map<QualifiedName, Object> compositePropertyValues = new HashMap<QualifiedName, Object>();
        for (Map.Entry<String, Object> stringObjectEntry : rawData.entrySet())
        {
            compositePropertyValues.put(QualifiedName.fromQN(stringObjectEntry.getKey()), stringObjectEntry.getValue());
        }

        return compositePropertyValues;
    }

    public final StateCommitter prepare(
            final Iterable<EntityState> newStates,
            final Iterable<EntityState> loadedStates,
            final Iterable<EntityReference> removedStates)
            throws EntityStoreException
    {
        checkActivation();

        config.startTransaction();

/*
        for( final EntityState state : newStates )
        {
            Map<QualifiedName, Object> properties = getProperties( state );
            properties.put( VERSION, 1 );
            properties.put( LASTMODIFIED, System.currentTimeMillis() );
            config.executeUpdate( "insert", state.identity(), properties );
        }
        for( final EntityState state : loadedStates )
        {
            Map<QualifiedName, Object> properties = getProperties( state );
            properties.put( VERSION, state.version() + 1 );
            properties.put( LASTMODIFIED, System.currentTimeMillis() );
            config.executeUpdate( "update", state.identity(), properties );
        }
        for( final EntityReference reference : removedStates )
        {
            config.executeUpdate( "delete", reference, reference.identity() );
        }
*/

        return config;
    }


    private Map<StateName, Object> getProperties(final EntityState state)
    {
/*
        final Map<StateName, Object> result = new HashMap<StateName, Object>();
        for( final PropertyType propertyName : state.propertyTypes() )
        {
            result.put( propertyName.qualifiedName(), state.getProperty( propertyName.qualifiedName() ) );
        }
        for( final AssociationType assocName : state.associationTypes() )
        {
            result.put( assocName.qualifiedName(), state.getAssociation( assocName.qualifiedName() ).identity() );
        }
        for( final ManyAssociationType manyAssocName : state.manyAssociationTypes() )
        {
            final ManyAssociationState manyAssociation = state.getManyAssociation( manyAssocName.qualifiedName() );
            result.put( manyAssocName.qualifiedName(), stringIdentifiersOf( manyAssociation ) );
        }
*/
        return null;
    }

    private Collection<String> stringIdentifiersOf(final ManyAssociationState entityReferences)
    {
        final Collection<String> identifiers = new ArrayList<String>(entityReferences.count());
        for (final EntityReference reference : entityReferences)
        {
            identifiers.add(reference.identity());
        }
        return identifiers;
    }

    public void visitEntityStates(EntityStateVisitor visitor)
    {
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
        config = new LegacySqlClient(configuration.sqlMapConfigURL().get(), configuration.configProperties().get());
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
        if (config != null)
        {
            config.passivate();
        }
    }
}
