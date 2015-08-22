/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.runtime.association;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.ManyAssociationWrapper;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.functional.Iterables;
import org.apache.zest.spi.entity.ManyAssociationState;

/**
 * JAVADOC
 */
public class ManyAssociationInstance<T>
    extends AbstractAssociationInstance<T>
    implements ManyAssociation<T>
{
    private ManyAssociationState manyAssociationState;

    public ManyAssociationInstance( AssociationInfo associationInfo,
                                    BiFunction<EntityReference, Type, Object> associationFunction,
                                    ManyAssociationState manyAssociationState
    )
    {
        super( associationInfo, associationFunction );
        this.manyAssociationState = manyAssociationState;
    }

    @Override
    public int count()
    {
        return manyAssociationState.count();
    }

    @Override
    public boolean contains( T entity )
    {
        return manyAssociationState.contains( getEntityReference( entity ) );
    }

    @Override
    public boolean add( int i, T entity )
    {
        NullArgumentException.validateNotNull( "entity", entity );
        checkImmutable();
        checkType( entity );
        associationInfo.checkConstraints( entity );
        return manyAssociationState.add( i, new EntityReference( ( (Identity) entity ).identity().get() ) );
    }

    @Override
    public boolean add( T entity )
    {
        return add( manyAssociationState.count(), entity );
    }

    @Override
    public boolean remove( T entity )
    {
        NullArgumentException.validateNotNull( "entity", entity );
        checkImmutable();
        checkType( entity );

        return manyAssociationState.remove( new EntityReference( ( (Identity) entity ).identity().get() ) );
    }

    @Override
    public T get( int i )
    {
        return getEntity( manyAssociationState.get( i ) );
    }

    @Override
    public List<T> toList()
    {
        ArrayList<T> list = new ArrayList<>();
        for( EntityReference entityReference : manyAssociationState )
        {
            list.add( getEntity( entityReference ) );
        }

        return list;
    }

    @Override
    public Set<T> toSet()
    {
        Set<T> set = new HashSet<>();
        for( EntityReference entityReference : manyAssociationState )
        {
            set.add( getEntity( entityReference ) );
        }

        return set;
    }

    @Override
    public Iterable<EntityReference> references()
    {
        return Iterables.toList( manyAssociationState );
    }

    @Override
    public String toString()
    {
        return manyAssociationState.toString();
    }

    @Override
    public Iterator<T> iterator()
    {
        return new ManyAssociationIterator( manyAssociationState.iterator() );
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
        ManyAssociation<?> that = (ManyAssociation) o;
        // Unwrap if needed
        while( that instanceof ManyAssociationWrapper )
        {
            that = ( (ManyAssociationWrapper) that ).next();
        }
        // Descriptor equality
        ManyAssociationInstance<?> thatInstance = (ManyAssociationInstance) that;
        AssociationDescriptor thatDescriptor = (AssociationDescriptor) thatInstance.associationInfo();
        if( !associationInfo.equals( thatDescriptor ) )
        {
            return false;
        }
        // State equality
        if( manyAssociationState.count() != thatInstance.manyAssociationState.count() )
        {
            return false;
        }
        for( EntityReference ref : manyAssociationState )
        {
            if( !thatInstance.manyAssociationState.contains( ref ) )
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
        for( EntityReference ref : manyAssociationState )
        {
            hash += ref.hashCode() * 7; // State
        }
        return hash;
    }

    public ManyAssociationState getManyAssociationState()
    {
        return manyAssociationState;
    }

    protected class ManyAssociationIterator
        implements Iterator<T>
    {
        private final Iterator<EntityReference> idIterator;

        public ManyAssociationIterator( Iterator<EntityReference> idIterator )
        {
            this.idIterator = idIterator;
        }

        @Override
        public boolean hasNext()
        {
            return idIterator.hasNext();
        }

        @Override
        public T next()
        {
            return getEntity( idIterator.next() );
        }

        @Override
        public void remove()
        {
            checkImmutable();
            idIterator.remove();
        }
    }
}
