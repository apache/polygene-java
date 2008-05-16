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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import static org.qi4j.spi.entity.EntityStatus.NEW;
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

    private final Map<String, Object> propertyValues;
    private final Map<String, QualifiedIdentity> associations;
    private int version;
    private EntityStatus status;

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
        CompositeDescriptor aDescriptor, QualifiedIdentity anIdentity,
        Map<String, Object> propertyValuez, int aVersion, EntityStatus aStatus
    )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "propertyValuez", propertyValuez );
        validateNotNull( "aStatus", aStatus );

        descriptor = aDescriptor;
        identity = anIdentity;
        status = aStatus;
        propertyValues = propertyValuez;
        version = aVersion;

        associations = new HashMap<String, QualifiedIdentity>();

        capitalizeKeys();
    }

    /**
     * Capitalize keys of the values. This is needed to ensure that regardless the backing database it will return
     * the right property names.
     *
     * @since 0.1.0
     */
    private void capitalizeKeys()
    {
        Set<String> keys = propertyValues.keySet();
        String[] keysArray = keys.toArray( new String[keys.size()] );
        for( String key : keysArray )
        {
            Object value = propertyValues.remove( key );
            String capitalizeKey = key.toUpperCase();
            propertyValues.put( capitalizeKey, value );
        }
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
     * @param aQualifiedName The property qualified name. This argument must not be {@code null}.
     * @return The property value given qualified name.
     * @since 0.2.0
     */
    public final Object getProperty( String aQualifiedName )
    {
        return propertyValues.get( aQualifiedName );
    }

    public void setProperty( String qualifiedName, Object newValue )
    {
        propertyValues.put( qualifiedName, newValue );
    }

    public QualifiedIdentity getAssociation( String aQualifiedName )
    {
        if( status == NEW || status == REMOVED )
        {
            return null;
        }

        if( !associations.containsKey( aQualifiedName ) )
        {
            // TODO
        }
        return associations.get( aQualifiedName );
    }

    public void setAssociation( String aQualifiedName, QualifiedIdentity newEntity )
    {
        associations.put( aQualifiedName, newEntity );
    }

    public Collection<QualifiedIdentity> getManyAssociation(
        String qualifiedName )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<QualifiedIdentity> setManyAssociation(
        String qualifiedName, Collection<QualifiedIdentity> newManyAssociation )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<String> getPropertyNames()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<String> getAssociationNames()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<String> getManyAssociationNames()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}