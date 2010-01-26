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
package org.qi4j.entitystore.qrm.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.entitystore.qrm.IdentifierConverter;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;

import static org.qi4j.api.util.NullArgumentException.*;
import static org.qi4j.spi.entity.EntityStatus.*;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link org.qi4j.spi.entity.EntityState}.
 */
public final class QrmEntityState
    //   implements EntityState, Serializable
{
    private static final long serialVersionUID = 1L;

    private final EntityType entityType;
    private final QualifiedIdentity identity;

    private final Map<QualifiedName, Object> propertyValues = new HashMap<QualifiedName, Object>();
    private final Map<QualifiedName, QualifiedIdentity> associations = new HashMap<QualifiedName, QualifiedIdentity>();
    private final Map<QualifiedName, Collection<QualifiedIdentity>> manyAssociations = new HashMap<QualifiedName, Collection<QualifiedIdentity>>();
    private long version;
    private long lastModified;
    private EntityStatus status;
    private IdentifierConverter identifierConverter = new CapitalizingIdentifierConverter();

    /**
     * Construct an instance of {@code IBatisEntityState}.
     *
     * @param entityType   The entityType of this State. This argument must not be {@code null}.
     * @param identity     The identity. This argument must not be {@code null}.
     * @param rawData      The field values of this entity state. This argument must not be {@code null}.
     * @param version      The version of the state.
     * @param status       The current status of the state. This argument must not be {@code null}.
     * @param lastModified The last modification date.
     *
     * @throws IllegalArgumentException if any of the following arguments are null; entityType, identity, rawData, status
     */
    public QrmEntityState(
        final EntityType entityType, final QualifiedIdentity identity,
        final Map<QualifiedName, Object> rawData,
        final long version, final long lastModified,
        final EntityStatus status
    )
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
        mapData( entityType, rawData );
        this.version = version;
        this.lastModified = lastModified;
    }

    private void mapData( final EntityType entityType, final Map<QualifiedName, Object> rawData )
    {
        Map<String, Object> convertedData = identifierConverter.convertKeys( rawData );
        System.err.println( rawData );
        System.err.println( convertedData );
        mapProperties( convertedData, entityType );
        mapAssociations( convertedData, entityType );
    }

    private void mapAssociations( final Map<String, Object> rawData, final EntityType stateDescriptor )
    {
        for( final AssociationType associationDescriptor : stateDescriptor.associations() )
        {
            final QualifiedName qualifiedName = associationDescriptor.qualifiedName();
            final String typeName = associationDescriptor.type().name();
            final String associationId = (String) identifierConverter.getValueFromData( rawData, qualifiedName );
            if( associationId != null )
            {
                setAssociation( qualifiedName, new QualifiedIdentity( associationId, typeName ) );
            }
        }

        for( final ManyAssociationType associationDescriptor : stateDescriptor.manyAssociations() )
        {
            final QualifiedName qualifiedName = associationDescriptor.qualifiedName();
            final String typeName = associationDescriptor.type();
            Collection<String> identifiers = (Collection<String>) identifierConverter.getValueFromData( rawData, qualifiedName );
            if( identifiers != null && !identifiers.isEmpty() )
            {
                setManyAssociation( qualifiedName, createQualifiedIdentities( identifiers, typeName, associationDescriptor ) );
            }
        }
    }

    private Collection<QualifiedIdentity> createQualifiedIdentities( final Collection<String> identifiers,
                                                                     final String typeName,
                                                                     ManyAssociationType associationType
    )
    {
        final int size = identifiers.size();
        final Collection<QualifiedIdentity> qualifiedIdentities = createManyAssociationCollection( size, associationType );
        for( String identifier : identifiers )
        {
            qualifiedIdentities.add( new QualifiedIdentity( identifier, typeName ) );
        }
        return qualifiedIdentities;
    }

    private Collection<QualifiedIdentity> createManyAssociationCollection( int size,
                                                                           ManyAssociationType associationType
    )
    {
        return new ArrayList<QualifiedIdentity>( size );
    }

    private void mapProperties( final Map<String, Object> rawData, final EntityType compositeModel )
    {
        for( final PropertyType propertyDescriptor : compositeModel.properties() )
        {
            final QualifiedName qualifiedName = propertyDescriptor.qualifiedName();
            final Object value = identifierConverter.getValueFromData( rawData, qualifiedName );
            setProperty( qualifiedName, convertValue( propertyDescriptor, value ) );
        }
    }

    private Object convertValue( final PropertyType propertyDescriptor, final Object value )
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
     *
     * @return The property value given qualified name.
     */
    public final Object getProperty( final QualifiedName qualifiedName )
    {
        return propertyValues.get( qualifiedName );
    }

    public void setProperty( final QualifiedName qualifiedName, final Object newValue )
    {
        propertyValues.put( qualifiedName, newValue );
    }

    public QualifiedIdentity getAssociation( final QualifiedName qualifiedName )
    {
        if( status == REMOVED )
        {
            return null;
        }

        if( !associations.containsKey( qualifiedName ) )
        {
            return null;
        }
        final QualifiedIdentity qualifiedIdentity = associations.get( qualifiedName );
        return qualifiedIdentity == null ? QualifiedIdentity.NULL : qualifiedIdentity;
    }

    public void setAssociation( final QualifiedName qualifiedName, final QualifiedIdentity qualifiedIdentity )
    {
        if( status == REMOVED )
        {
            throw new EntityNotFoundException( null );
        }
        associations.put( qualifiedName, qualifiedIdentity != null ? qualifiedIdentity : QualifiedIdentity.NULL );
    }

    public Collection<QualifiedIdentity> getManyAssociation( final QualifiedName qualifiedName )
    {
        if( status == REMOVED )
        {
            return null;
        }

        return manyAssociations.get( qualifiedName );
    }

    public Collection<QualifiedIdentity> setManyAssociation(
        final QualifiedName qualifiedName, final Collection<QualifiedIdentity> newManyAssociations
    )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        if( status == REMOVED )
        {
            throw new EntityNotFoundException( null );
        }
        return manyAssociations.put( qualifiedName, newManyAssociations );
    }

    public String convertIdentifier( final QualifiedName qualifiedIdentifier )
    {
        return identifierConverter.convertIdentifier( qualifiedIdentifier );
    }

    public final Iterable<QualifiedName> propertyNames()
    {
        return Collections.unmodifiableSet( propertyValues.keySet() );
    }

    public final Iterable<QualifiedName> associationNames()
    {
        return Collections.unmodifiableSet( associations.keySet() );
    }

    public final Iterable<QualifiedName> manyAssociationNames()
    {
        return Collections.unmodifiableSet( manyAssociations.keySet() );
    }

    public Map<QualifiedName, Object> getPropertyValues()
    {
        return propertyValues;
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version++;
    }
}