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

package org.qi4j.index.sql.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONStringer;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.Value;
import org.qi4j.index.sql.IndexingConfiguration;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class EntityStateSqlSerializer
{
    private UuidIdentityGeneratorService idGenerator;
    private PreparedStatement preparedStatementForAssociations;
    private PreparedStatement preparedStatementForProperty;
    private PreparedStatement preparedStatementForValues;

    public EntityStateSqlSerializer( Connection conn,
                                     IndexingConfiguration conf,
                                     UuidIdentityGeneratorService idGenerator
    )
        throws SQLException
    {
        this.idGenerator = idGenerator;
        String sql1 = conf.insertAssociation().get();
        if( sql1 == null )
        {
            sql1 = "INSERT INTO QI_ASSOCIATIONS set ENTITY_ID = ?, set ASSOC_NAME = ?, set REF_ID = ?";
        }
        preparedStatementForAssociations = conn.prepareStatement( sql1 );
        String sql2 = conf.insertProperty().get();
        if( sql2 == null )
        {
            sql2 = "INSERT INTO QI_PROPERTIES set PROPERTY_ID = ?, set PROPERTY_NAME = ?, set PROPERTY_TYPE = ? , set PROPERTY_DATA = ?";
        }
        preparedStatementForProperty = conn.prepareStatement( sql2 );
        String sql3 = conf.insertValue().get();
        if( sql3 == null )
        {
            sql3 = "INSERT INTO QI_VALUES set VALUE_ID = ?, set VALUE_TYPE = ?, set VALUE_DATA = ?";
        }
        preparedStatementForValues = conn.prepareStatement( sql3 );
    }

    private void serialize( final EntityState entityState,
                            final boolean includeNonQueryable
    )
        throws SQLException
    {
        EntityType entityType = entityState.entityDescriptor().entityType();
        serializeProperties( entityState, entityType, includeNonQueryable );

        serializeAssociations( entityState, entityType.associations(), includeNonQueryable );
        serializeManyAssociations( entityState, entityType.manyAssociations(), includeNonQueryable );
    }

    private void serializeProperties( final EntityState entityState,
                                      final EntityType entityType,
                                      final boolean includeNonQueryable
    )
        throws SQLException
    {
        try
        {
            // Properties
            for( PropertyType propertyType : entityType.properties() )
            {
                Object property = entityState.getProperty( propertyType.qualifiedName() );
                if( property != null )
                {
                    serializeProperty( propertyType, property, includeNonQueryable );
                }
            }
        }
        catch( JSONException e )
        {
            throw new IllegalArgumentException( "Could not JSON serialize value", e );
        }
    }

    private String serializeProperty( PropertyType propertyType,
                                      Object property,
                                      boolean includeNonQueryable
    )
        throws JSONException, SQLException
    {
        if( !( includeNonQueryable || propertyType.queryable() ) )
        {
            return null; // Skip non-queryable
        }

        ValueType valueType = propertyType.type();
        String propertyName = propertyType.qualifiedName().toURI();

        String value;
        if( valueType.isValue() )
        {
            value = serializeValueComposite( (Value) property, valueType, includeNonQueryable );
        }
        else
        {
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.array();
            valueType.toJSON( property, jsonStringer );
            jsonStringer.endArray();
            value = jsonStringer.toString();

            if( valueType.isString() ) // Remove "" around strings
            {
                value = value.substring( 2, value.length() - 2 );
            }
            else
            {
                value = value.substring( 1, value.length() - 1 );
            }
        }
        String id = idGenerator.generate( UuidIdentityGeneratorService.class );
        preparedStatementForProperty.setString( 1, id );
        preparedStatementForProperty.setString( 2, propertyName );
        preparedStatementForProperty.setString( 3, propertyType.qualifiedName().toString() );
        preparedStatementForProperty.setString( 4, value );
        preparedStatementForProperty.execute();
        return id;
    }

    private String serializeValueComposite( Value value,
                                            ValueType valueType,
                                            boolean includeNonQueryable
    )
        throws JSONException, SQLException
    {
        String id = idGenerator.generate( UuidIdentityGeneratorService.class );
        for( PropertyType propertyType : valueType.types() )
        {
            Object propertyValue = value.state().getProperty( propertyType.qualifiedName() ).get();

            if( propertyValue == null )
            {
                continue; // Skip null values
            }

            ValueType type = propertyType.type();
            String valueField;
            if( type.isValue() )
            {
                valueField = serializeValueComposite( (Value) propertyValue, type, includeNonQueryable );
            }
            else
            {
                valueField = serializeProperty( propertyType, propertyValue, includeNonQueryable );
            }
            preparedStatementForValues.setString( 1, id );
            preparedStatementForValues.setString( 2, propertyType.qualifiedName().toString() );
            preparedStatementForValues.setString( 3, valueField );
            preparedStatementForValues.execute();
        }
        return id;
    }

    private void serializeAssociations( final EntityState entityState,
                                        final Iterable<AssociationType> associations,
                                        final boolean includeNonQueryable
    )
        throws SQLException
    {
        // Many-Associations
        for( AssociationType associationType : associations )
        {
            if( !( includeNonQueryable || associationType.queryable() ) )
            {
                continue; // Skip non-queryable
            }
            String entityId = entityState.identity().identity();
            QualifiedName qualifiedName = associationType.qualifiedName();
            EntityReference association = entityState.getAssociation( qualifiedName );
            preparedStatementForAssociations.setString( 1, entityId );
            preparedStatementForAssociations.setString( 2, qualifiedName.toString() );
            preparedStatementForAssociations.setString( 3, association.identity() );
            preparedStatementForAssociations.execute();
        }
    }

    private void serializeManyAssociations( final EntityState entityState,
                                            final Iterable<ManyAssociationType> associations,
                                            final boolean includeNonQueryable
    )
        throws SQLException
    {
        // Many-Associations
        for( ManyAssociationType associationType : associations )
        {
            if( !( includeNonQueryable || associationType.queryable() ) )
            {
                continue; // Skip non-queryable
            }
            String entityId = entityState.identity().identity();
            QualifiedName qualifiedName = associationType.qualifiedName();
            ManyAssociationState associatedIds = entityState.getManyAssociation( qualifiedName );
            for( EntityReference associatedId : associatedIds )
            {
                preparedStatementForAssociations.setString( 1, entityId );
                preparedStatementForAssociations.setString( 2, qualifiedName.toString() );
                preparedStatementForAssociations.setString( 3, associatedId.identity() );
                preparedStatementForAssociations.execute();
            }
        }
    }
}
