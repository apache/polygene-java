/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.association;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.association.NamedAssociationWrapper;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.util.NullArgumentException;
import org.apache.polygene.spi.entity.NamedAssociationState;

public class NamedAssociationInstance<T>
    extends AbstractAssociationInstance<T>
    implements NamedAssociation<T>
{

    private final NamedAssociationState namedAssociationState;

    public NamedAssociationInstance( AssociationInfo associationInfo,
                                     BiFunction<EntityReference, Type, Object> associationFunction,
                                     NamedAssociationState namedAssociationState
    )
    {
        super( associationInfo, associationFunction );
        this.namedAssociationState = namedAssociationState;
    }

    @Override
    public Iterator<String> iterator()
    {
        return namedAssociationState.iterator();
    }

    @Override
    public int count()
    {
        return namedAssociationState.count();
    }

    @Override
    public boolean containsName( String name )
    {
        return namedAssociationState.containsName( name );
    }

    @Override
    public boolean put( String name, T entity )
    {
        NullArgumentException.validateNotNull( "entity", entity );
        checkImmutable();
        checkType( entity );
        associationInfo.checkConstraints( entity );
        return namedAssociationState.put( name, EntityReference.create( ((HasIdentity) entity).identity().get() ) );
    }

    @Override
    public boolean remove( String name )
    {
        checkImmutable();
        return namedAssociationState.remove( name );
    }

    @Override
    public T get( String name )
    {
        return getEntity( namedAssociationState.get( name ) );
    }

    @Override
    public String nameOf( T entity )
    {
        return namedAssociationState.nameOf( getEntityReference( entity ) );
    }

    @Override
    public Map<String, T> toMap()
    {
        Map<String, T> map = new HashMap<>();
        for( String name : namedAssociationState )
        {
            map.put( name, getEntity( namedAssociationState.get( name ) ) );
        }
        return map;
    }

    @Override
    public Stream<Map.Entry<String, EntityReference>> references()
    {
        return namedAssociationState.stream();
    }

    @Override
    public EntityReference referenceOf( String name )
    {
        return namedAssociationState.get( name );
    }

    public Iterable<Map.Entry<String, EntityReference>> getEntityReferences()
    {
        return Collections.unmodifiableMap(
            StreamSupport.stream( namedAssociationState.spliterator(), false )
                         .collect( Collectors.toMap( Function.identity(), namedAssociationState::get ) )
        ).entrySet();
    }


    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        NamedAssociation<?> that = (NamedAssociation) o;
        // Unwrap if needed
        while( that instanceof NamedAssociationWrapper )
        {
            that = ( (NamedAssociationWrapper) that ).next();
        }
        // Descriptor equality
        NamedAssociationInstance<?> thatInstance = (NamedAssociationInstance) that;
        AssociationDescriptor thatDescriptor = (AssociationDescriptor) thatInstance.associationInfo();
        if( !associationInfo.equals( thatDescriptor ) )
        {
            return false;
        }
        // State equality
        if( namedAssociationState.count() != thatInstance.namedAssociationState.count() )
        {
            return false;
        }
        for( String name : namedAssociationState )
        {
            if( !thatInstance.namedAssociationState.containsName( name ) )
            {
                return false;
            }
            EntityReference thisReference = namedAssociationState.get( name );
            EntityReference thatReference = thatInstance.namedAssociationState.get( name );
            if( !thisReference.equals( thatReference ) )
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = associationInfo.hashCode() * 31; // Descriptor
        for( String name : namedAssociationState )
        {
            hash += name.hashCode();
            hash += namedAssociationState.get( name ).hashCode() * 7; // State
        }
        return hash;
    }

}
