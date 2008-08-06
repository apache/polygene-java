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
package org.qi4j.entity.ibatis.internal;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.ibatis.IbatisCompositeBuilder;
import org.qi4j.entity.ibatis.IdentifierConverter;
import org.qi4j.spi.entity.AssociationType;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import static org.qi4j.spi.entity.EntityStatus.REMOVED;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationType;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link org.qi4j.spi.entity.EntityState}.
 */
public final class IBatisEntityState
    implements EntityState, Serializable
{
    private static final long serialVersionUID = 1L;

    private final EntityType entityType;
    private final QualifiedIdentity identity;

    private final Map<String, Object> propertyValues = new HashMap<String, Object>();
    private final Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
    private final Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
    private long version;
    private long lastModified;
    private EntityStatus status;
    private IdentifierConverter identifierConverter = new CapitalizingIdentifierConverter();

    /**
     * Construct an instance of {@code IBatisEntityState}.
     *
     * @param entityType
     * @param identity
     * @param rawData                The field values of this entity state. This argument must not be {@code null}.
     * @param version
     * @param status
     * @param ibatisCompositeBuilder
     */
    public IBatisEntityState(
        final EntityType entityType, final QualifiedIdentity identity,
        final Map<String, Object> rawData,
        final long version, final long lastModified,
        final EntityStatus status,
        final IbatisCompositeBuilder ibatisCompositeBuilder )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", entityType );
        validateNotNull( "anIdentity", identity );
        validateNotNull( "propertyValuez", rawData );
        validateNotNull( "aStatus", status );
        // TODO validateNotNull( "aVersion", aVersion );

        this.entityType = entityType;
        this.identity = identity;
        this.status = status;
        mapData( entityType, rawData, ibatisCompositeBuilder );
        this.version = version;
        this.lastModified = lastModified;
    }

    private void mapData( final EntityType entityType, final Map<String, Object> rawData, final IbatisCompositeBuilder ibatisCompositeBuilder )
    {
        Map<String, Object> convertedData = identifierConverter.convertKeys( rawData );
        System.err.println( rawData );
        System.err.println( convertedData );
        mapProperties( convertedData, entityType, ibatisCompositeBuilder );
        mapAssociations( convertedData, entityType );
    }

    private void mapAssociations( final Map<String, Object> rawData, final EntityType stateDescriptor )
    {
        for( final AssociationType associationDescriptor : stateDescriptor.associations() )
        {
            final String qualifiedName = associationDescriptor.qualifiedName();
            final String typeName = associationDescriptor.type();
            final String associationId = (String) identifierConverter.getValueFromData( rawData, qualifiedName );
            if( associationId != null )
            {
                setAssociation( qualifiedName, new QualifiedIdentity( associationId, typeName ) );
            }
        }

        for( final ManyAssociationType associationDescriptor : stateDescriptor.manyAssociations() )
        {
            final String qualifiedName = associationDescriptor.qualifiedName();
            final String typeName = associationDescriptor.type();
            Collection<String> identifiers = (Collection<String>) identifierConverter.getValueFromData( rawData, qualifiedName );
            if( identifiers != null && !identifiers.isEmpty() )
            {
                setManyAssociation( qualifiedName, createQualifiedIdentities( identifiers, typeName, associationDescriptor ) );
            }
        }
    }

    private Collection<QualifiedIdentity> createQualifiedIdentities( final Collection<String> identifiers, final String typeName, ManyAssociationType associationType )
    {
        final int size = identifiers.size();
        final Collection<QualifiedIdentity> qualifiedIdentities = createManyAssociationCollection( size, associationType );
        for( String identifier : identifiers )
        {
            qualifiedIdentities.add( new QualifiedIdentity( identifier, typeName ) );
        }
        return qualifiedIdentities;
    }

    private Collection<QualifiedIdentity> createManyAssociationCollection( int size, ManyAssociationType associationType )
    {
        if( associationType.associationType() == ManyAssociationType.ManyAssociationTypeEnum.SET )
        {
            return new HashSet<QualifiedIdentity>( size );
        }
        else
        {
            return new ArrayList<QualifiedIdentity>( size );
        }
    }

    private void mapProperties( final Map<String, Object> rawData, final EntityType compositeModel, final IbatisCompositeBuilder ibatisCompositeBuilder )
    {
        for( final PropertyType propertyDescriptor : compositeModel.properties() )
        {
            final String qualifiedName = propertyDescriptor.qualifiedName();
            final Object value = identifierConverter.getValueFromData( rawData, qualifiedName );
            setProperty( qualifiedName, convertValue( propertyDescriptor, value, ibatisCompositeBuilder ) );
        }
    }

    private Object convertValue( final PropertyType propertyDescriptor, final Object value, final IbatisCompositeBuilder ibatisCompositeBuilder )
    {
        return value; // TODO Implement value conversion
    }

    private Class getPropertyTypeClass( final PropertyDescriptor propertyModel )
    {
        if( propertyModel.type() instanceof Class )
        {
            return (Class) propertyModel.type();

        }
        return null;
    }

    private String getTypeName( final AssociationDescriptor associationModel )
    {
        final Type associationType = associationModel.type();
        if( associationType instanceof Class )
        {
            final Class type = (Class) associationType;
            return type.getName();
        }
        return associationType.toString();
    }

    /**
     * Returns the identity of the entity that this EntityState represents.
     *
     * @return the identity of the entity that this EntityState represents.
     * @since 0.2.0
     */
    public QualifiedIdentity qualifiedIdentity()
    {
        return identity;
    }

    public long version()
    {
        return version;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public void remove()
    {
        status = REMOVED;
    }

    /**
     * Returns the status of entity represented by this entity state.
     *
     * @return The status of entity represented by this entity state.
     * @since 0.2.0
     */
    public final EntityStatus status()
    {
        return status;
    }

    public EntityType entityType()
    {
        return entityType;
    }

    /**
     * Returns the property value given the property qualified name.
     *
     * @param qualifiedName The property qualified name. This argument must not be {@code null}.
     * @return The property value given qualified name.
     * @since 0.2.0
     */
    public final Object getProperty( final String qualifiedName )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        return propertyValues.get( convertIdentifier( qualifiedName ) );
    }

    public void setProperty( final String qualifiedName, final Object newValue )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        propertyValues.put( convertIdentifier( qualifiedName ), newValue );
    }

    public QualifiedIdentity getAssociation( final String qualifiedName )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        if( status == REMOVED )
        {
            return null;
        }

        final String convertedIdentifier = convertIdentifier( qualifiedName );
        if( !associations.containsKey( convertedIdentifier ) )
        {
            return null;
        }
        final QualifiedIdentity qualifiedIdentity = associations.get( convertedIdentifier );
        return qualifiedIdentity == null ? QualifiedIdentity.NULL : qualifiedIdentity;
    }

    public void setAssociation( final String qualifiedName, final QualifiedIdentity qualifiedIdentity )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        if( status == REMOVED )
        {
            throw new EntityNotFoundException( "IbatisEntityStore", qualifiedIdentity().identity() );
        }
        final String convertedIdentifier = convertIdentifier( qualifiedName );
        associations.put( convertedIdentifier, qualifiedIdentity != null ? qualifiedIdentity : QualifiedIdentity.NULL );
    }

    public Collection<QualifiedIdentity> getManyAssociation( final String qualifiedName )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        if( status == REMOVED )
        {
            return null;
        }

        return manyAssociations.get( convertIdentifier( qualifiedName ) );
    }

    public Collection<QualifiedIdentity> setManyAssociation(
        final String qualifiedName, final Collection<QualifiedIdentity> newManyAssociations )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        if( status == REMOVED )
        {
            throw new EntityNotFoundException( "IbatisEntityStore", qualifiedIdentity().identity() );
        }
        final String convertedIdentifier = convertIdentifier( qualifiedName );
        return manyAssociations.put( convertedIdentifier, newManyAssociations );
    }

    public String convertIdentifier( final String qualifiedIdentifier )
    {
        return identifierConverter.convertIdentifier( qualifiedIdentifier );
    }

    public final Iterable<String> propertyNames()
    {
        return Collections.unmodifiableSet( propertyValues.keySet() );
    }

    public final Iterable<String> associationNames()
    {
        return Collections.unmodifiableSet( associations.keySet() );
    }

    public final Iterable<String> manyAssociationNames()
    {
        return Collections.unmodifiableSet( manyAssociations.keySet() );
    }

    public Map<String, Object> getPropertyValues()
    {
        return propertyValues;
    }
}