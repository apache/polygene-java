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
package org.qi4j.entitystore.legacy.internal;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.entitystore.legacy.IdentifierConverter;
import org.qi4j.spi.entity.*;
import static org.qi4j.spi.entity.EntityStatus.REMOVED;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.helpers.DefaultManyAssociationState;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link org.qi4j.spi.entity.EntityState}.
 */
public final class LegacyEntityState
        implements EntityState, Serializable
{
    private static final long serialVersionUID = 1L;

    private final Set<EntityTypeReference> entityTypes;
    private final EntityReference reference;

    private final Map<StateName, String> propertyValues = new HashMap<StateName, String>();
    private final Map<StateName, EntityReference> associations = new HashMap<StateName, EntityReference>();
    private final Map<StateName, ManyAssociationState> manyAssociations = new HashMap<StateName, ManyAssociationState>();
    private long version;
    private long lastModified;
    private EntityStatus status;
    private IdentifierConverter identifierConverter = new CapitalizingIdentifierConverter();

    /**
     * Construct an instance of {@code IBatisEntityState}.
     *
     * @param reference
     * @param rawData   The field values of this entity state. This argument must not be {@code null}.
     * @param version
     * @param status
     */
    public LegacyEntityState(
            final Set<EntityTypeReference> entityTypes, final EntityReference reference,
            final Map<QualifiedName, Object> rawData,
            final long version, final long lastModified,
            final EntityStatus status)
            throws IllegalArgumentException
    {
        validateNotNull("anIdentity", reference);
        validateNotNull("propertyValuez", rawData);
        validateNotNull("aStatus", status);
        // TODO validateNotNull( "aVersion", aVersion );

        this.entityTypes = entityTypes;
        this.reference = reference;
        this.status = status;
        mapData(entityTypes, rawData);
        this.version = version;
        this.lastModified = lastModified;
    }

    private void mapData(final Set<EntityTypeReference> entityTypes, final Map<QualifiedName, Object> rawData)
    {
        Map<String, Object> convertedData = identifierConverter.convertKeys(rawData);
        System.err.println(rawData);
        System.err.println(convertedData);
        mapProperties(convertedData);
        mapAssociations(convertedData);
    }

    private void mapAssociations(final Map<String, Object> rawData)
    {
/* TODO
        for( final AssociationType associationDescriptor : associationTypes() )
        {
            final QualifiedName qualifiedName = associationDescriptor.qualifiedName();
            final TypeName typeName = associationDescriptor.type();
            final String associationId = (String) identifierConverter.getValueFromData( rawData, qualifiedName );
            if( associationId != null )
            {
                setAssociation( qualifiedName, new EntityReference( associationId ) );
            }
        }

        for( final ManyAssociationType associationDescriptor : manyAssociationTypes() )
        {
            final QualifiedName qualifiedName = associationDescriptor.qualifiedName();
            final String typeName = associationDescriptor.type();
            Collection<String> identifiers = (Collection<String>) identifierConverter.getValueFromData( rawData, qualifiedName );
            if( identifiers != null && !identifiers.isEmpty() )
            {
                setManyAssociation( qualifiedName, createQualifiedIdentities( identifiers) );
            }
        }
*/
    }

    private ManyAssociationState createQualifiedIdentities(final Collection<String> identifiers)
    {
        final ManyAssociationState entityReferences = new DefaultManyAssociationState();
        for (String identifier : identifiers)
        {
            entityReferences.add(entityReferences.count(), EntityReference.parseEntityReference(identifier));
        }
        return entityReferences;
    }

    private void mapProperties(final Map<String, Object> rawData)
    {
/* TODO
        for( final PropertyType propertyDescriptor : propertyTypes() )
        {
            final QualifiedName qualifiedName = propertyDescriptor.qualifiedName();
            final Object value = identifierConverter.getValueFromData( rawData, qualifiedName );
            setProperty( qualifiedName, convertValue( propertyDescriptor, value ) );
        }
*/
    }

    private Object convertValue(final PropertyType propertyDescriptor, final Object value)
    {
        return value; // TODO Implement value conversion
    }

    private Class getPropertyTypeClass(final PropertyDescriptor propertyModel)
    {
        if (propertyModel.type() instanceof Class)
        {
            return (Class) propertyModel.type();

        }
        return null;
    }

    private String getTypeName(final AssociationDescriptor associationModel)
    {
        final Type associationType = associationModel.type();
        if (associationType instanceof Class)
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
    public EntityReference identity()
    {
        return reference;
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

    public void addEntityTypeReference(EntityTypeReference type)
    {
        entityTypes.add(type);
    }

    public void removeEntityTypeReference(EntityTypeReference type)
    {
        // TODO ?
    }

    public boolean hasEntityTypeReference(EntityTypeReference type)
    {
        return entityTypes.add(type);
    }

    public Set<EntityTypeReference> entityTypeReferences()
    {
        return entityTypes;
    }

    /**
     * Returns the property value given the property qualified name.
     *
     * @param qualifiedName The property qualified name. This argument must not be {@code null}.
     * @return The property value given qualified name.
     * @since 0.2.0
     */
    public final String getProperty(final StateName qualifiedName)
    {
        return propertyValues.get(qualifiedName);
    }

    public void setProperty(final StateName qualifiedName, final String newValue)
    {
        propertyValues.put(qualifiedName, newValue);
    }

    public EntityReference getAssociation(final StateName qualifiedName)
    {
        if (status == REMOVED)
        {
            return null;
        }

        if (!associations.containsKey(qualifiedName))
        {
            return null;
        }
        final EntityReference entityReference = associations.get(qualifiedName);
        return entityReference == null ? EntityReference.NULL : entityReference;
    }

    public void setAssociation(final StateName qualifiedName, final EntityReference entityReference)
    {
        if (status == REMOVED)
        {
            throw new EntityNotFoundException(identity());
        }
        associations.put(qualifiedName, entityReference != null ? entityReference : EntityReference.NULL);
    }

    public ManyAssociationState getManyAssociation(final StateName qualifiedName)
    {
        if (status == REMOVED)
        {
            return null;
        }

        return manyAssociations.get(qualifiedName);
    }

    public ManyAssociationState setManyAssociation(
            final StateName qualifiedName, final ManyAssociationState newManyAssociations)
    {
        validateNotNull("stateName", qualifiedName);
        if (status == REMOVED)
        {
            throw new EntityNotFoundException(identity());
        }
        return manyAssociations.put(qualifiedName, newManyAssociations);
    }

    public String convertIdentifier(final QualifiedName qualifiedIdentifier)
    {
        return identifierConverter.convertIdentifier(qualifiedIdentifier);
    }

    public Map<StateName, String> getPropertyValues()
    {
        return propertyValues;
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version++;
    }
}