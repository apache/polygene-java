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
import static org.qi4j.composite.NullArgumentException.*;
import org.qi4j.entity.association.ImmutableAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.entity.ibatis.IbatisCompositeBuilder;
import org.qi4j.entity.ibatis.IdentifierConverter;
import org.qi4j.entity.EntityComposite;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import static org.qi4j.spi.entity.EntityStatus.*;
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

    private final CompositeDescriptor descriptor;
    private final QualifiedIdentity identity;

    private final Map<String, Object> propertyValues = new HashMap<String, Object>();
    private final Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
    private final Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
    private Long version;
    private EntityStatus status;
    private IdentifierConverter identifierConverter = new CapitalizingIdentifierConverter();

    /**
     * Construct an instance of {@code IBatisEntityState}.
     *
     * @param descriptor
     * @param identity
     * @param rawData                The field values of this entity state. This argument must not be {@code null}.
     * @param version
     * @param status
     * @param ibatisCompositeBuilder
     */
    public IBatisEntityState(
        final CompositeDescriptor descriptor, final QualifiedIdentity identity,
        final Map<String, Object> rawData, final Long version, final EntityStatus status,
        final IbatisCompositeBuilder ibatisCompositeBuilder )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", descriptor );
        validateNotNull( "anIdentity", identity );
        validateNotNull( "propertyValuez", rawData );
        validateNotNull( "aStatus", status );
        // TODO validateNotNull( "aVersion", aVersion );

        this.descriptor = descriptor;
        this.identity = identity;
        this.status = status;
        mapData( descriptor, rawData, ibatisCompositeBuilder );
        this.version = version;
    }

    private void mapData( final CompositeDescriptor compositeDescriptor, final Map<String, Object> rawData, final IbatisCompositeBuilder ibatisCompositeBuilder )
    {
        final StateDescriptor stateDescriptor = compositeDescriptor.state();
        Map<String, Object> convertedData = identifierConverter.convertKeys( rawData );
        System.err.println( rawData );
        System.err.println( convertedData );
        mapProperties( convertedData, stateDescriptor, ibatisCompositeBuilder );
        mapAssociations( convertedData, stateDescriptor );
    }

    private void mapAssociations( final Map<String, Object> rawData, final StateDescriptor stateDescriptor )
    {

        for( final AssociationDescriptor associationDescriptor : stateDescriptor.associations() )
        {
            final String qualifiedName = associationDescriptor.qualifiedName();
            final String typeName = getTypeName( associationDescriptor );
            final Class<?> associationType = associationDescriptor.accessor().getReturnType();
            if( ManyAssociation.class.isAssignableFrom( associationType ) )
            {
                Collection<String> identifiers = (Collection<String>) identifierConverter.getValueFromData( rawData, qualifiedName );
                if( identifiers != null && !identifiers.isEmpty() )
                {
                    setManyAssociation( qualifiedName, createQualifiedIdentities( identifiers, typeName, associationType ) );
                }
            }
            else
            {
                final String associationId = (String) identifierConverter.getValueFromData( rawData, qualifiedName );
                if( associationId != null )
                {
                    setAssociation( qualifiedName, new QualifiedIdentity( associationId, typeName ) );
                }
            }
        }
    }

    private Collection<QualifiedIdentity> createQualifiedIdentities( final Collection<String> identifiers, final String typeName, Class<?> associationType )
    {
        final int size = identifiers.size();
        final Collection<QualifiedIdentity> qualifiedIdentities = createManyAssociationCollection( size, associationType );
        for( String identifier : identifiers )
        {
            qualifiedIdentities.add( new QualifiedIdentity( identifier, typeName ) );
        }
        if( ImmutableAssociation.class.isAssignableFrom( associationType ) )
        {
            return Collections.unmodifiableCollection( qualifiedIdentities );
        }
        return qualifiedIdentities;
    }

    private Collection<QualifiedIdentity> createManyAssociationCollection( int size, Class<?> associationType )
    {
        if( SetAssociation.class.isAssignableFrom( associationType ) )
        {
            return new HashSet<QualifiedIdentity>( size );
        }
        return new ArrayList<QualifiedIdentity>( size );
    }

    private void mapProperties( final Map<String, Object> rawData, final StateDescriptor compositeModel, final IbatisCompositeBuilder ibatisCompositeBuilder )
    {
        for( final PropertyDescriptor propertyDescriptor : compositeModel.properties() )
        {
            final String qualifiedName = propertyDescriptor.qualifiedName();
            final Object value = identifierConverter.getValueFromData( rawData, qualifiedName );
            if( value != null )
            {
                setProperty( qualifiedName, convertValue( propertyDescriptor, value, ibatisCompositeBuilder ) );
            } else {
                setProperty( qualifiedName, propertyDescriptor.defaultValue() );
            }
        }
    }

    private Object convertValue( final PropertyDescriptor propertyDescriptor, final Object value, final IbatisCompositeBuilder ibatisCompositeBuilder )
    {
        final Class propertyClass = getPropertyTypeClass( propertyDescriptor );
        if( propertyClass == null || value == null || propertyClass.isInstance( value ) )
        {
            return value;
        }
        if( Map.class.isAssignableFrom( value.getClass() ) && EntityComposite.class.isAssignableFrom( propertyClass ) )
        {

            return ibatisCompositeBuilder.createEntityComposite( (Map<String, Object>) value, propertyClass );

        }
        return propertyClass.cast( value );
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
    public QualifiedIdentity getIdentity()
    {
        return identity;
    }

    public long getEntityVersion()
    {
        return version;
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
    public final EntityStatus getStatus()
    {
        return status;
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
            throw new EntityNotFoundException( "IbatisEntityStore", getIdentity().identity() );
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
            throw new EntityNotFoundException( "IbatisEntityStore", getIdentity().identity() );
        }
        final String convertedIdentifier = convertIdentifier( qualifiedName );
        return manyAssociations.put( convertedIdentifier, newManyAssociations );
    }

    public String convertIdentifier( final String qualifiedIdentifier )
    {
        return identifierConverter.convertIdentifier( qualifiedIdentifier );
    }

    public final Iterable<String> getPropertyNames()
    {
        return Collections.unmodifiableSet( propertyValues.keySet() );
    }

    public final Iterable<String> getAssociationNames()
    {
        return Collections.unmodifiableSet( associations.keySet() );
    }

    public final Iterable<String> getManyAssociationNames()
    {
        return Collections.unmodifiableSet( manyAssociations.keySet() );
    }

    public Map<String, Object> getPropertyValues()
    {
        return propertyValues;
    }
}