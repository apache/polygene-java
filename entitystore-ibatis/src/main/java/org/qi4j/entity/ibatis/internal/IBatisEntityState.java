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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.ibatis.IdentifierConverter;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import static org.qi4j.spi.entity.EntityStatus.REMOVED;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.structure.CompositeDescriptor;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link org.qi4j.spi.entity.EntityState}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
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
     * @param aDescriptor    The composite descriptor. This argument must not be {@code null}.
     * @param anIdentity     The identity of the composite that this {@code IBatisEntityState} represents.
     *                       This argument must not be {@code null}.
     * @param propertyValuez The field values of this entity state. This argument must not be {@code null}.
     * @param aVersion       The version.
     * @param aStatus        The initial entity state status. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @since 0.1.0
     */
    public IBatisEntityState(
        final CompositeDescriptor aDescriptor, final QualifiedIdentity anIdentity,
        final Map<String, Object> propertyValuez, final Long aVersion, final EntityStatus aStatus
    )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "propertyValuez", propertyValuez );
        validateNotNull( "aStatus", aStatus );
        // TODO validateNotNull( "aVersion", aVersion );

        descriptor = aDescriptor;
        identity = anIdentity;
        status = aStatus;
        propertyValues.putAll( convertKeys( propertyValuez ) );
        version = aVersion;

    }

    /**
     * Capitalize keys of the values. This is needed to ensure that regardless the backing database it will return
     * the right property names.
     *
     * @param columnValueMap
     * @since 0.1.0
     */
    private <T> Map<String, T> convertKeys( final Map<String, T> columnValueMap )
    {
        final Map<String, T> result = new HashMap<String, T>( columnValueMap.size() );
        for( final Map.Entry<String, T> entry : columnValueMap.entrySet() )
        {
            result.put( convertIdentifier( entry.getKey() ), entry.getValue() );
        }
        return result;
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

        final QualifiedIdentity qualifiedIdentity = associations.get( convertIdentifier( qualifiedName ) );
        if( qualifiedIdentity == null )
        {
            return QualifiedIdentity.NULL; // todo mandatory
        }
        return associations.get( qualifiedName );
    }

    public void setAssociation( final String qualifiedName, final QualifiedIdentity qualifiedIdentity )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        if( status == REMOVED )
        {
            throw new EntityNotFoundException( "IbatisEntityStore", getIdentity().getIdentity() );
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

        final Collection<QualifiedIdentity> identities = manyAssociations.get( convertIdentifier( qualifiedName ) );
        if( identities == null )
        {
            return Collections.emptyList();
        }
        return identities;
    }

    public Collection<QualifiedIdentity> setManyAssociation(
        final String qualifiedName, final Collection<QualifiedIdentity> newManyAssociations )
    {
        validateNotNull( "qualifiedName", qualifiedName );
        if( status == REMOVED )
        {
            throw new EntityNotFoundException( "IbatisEntityStore", getIdentity().getIdentity() );
        }
        final String convertedIdentifier = convertIdentifier( qualifiedName );
        if( newManyAssociations == null )
        {
            manyAssociations.put( convertedIdentifier, Collections.singletonList( QualifiedIdentity.NULL ) ); // todo ??
        }

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