/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.spi.entity.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.value.ValueState;

/**
 * Standard implementation of EntityState.
 */
public class DefaultEntityState
    implements EntityState, Serializable
{
    public static Map<String, Collection<QualifiedIdentity>> newManyCollections( EntityType entityType )
    {
        Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
        for( ManyAssociationType manyAssociationType : entityType.manyAssociations() )
        {
            switch( manyAssociationType.associationType() )
            {
            case LIST:
            {
                manyAssociations.put( manyAssociationType.qualifiedName(), new ArrayList<QualifiedIdentity>() );
                break;
            }
            case SET:
            {
                manyAssociations.put( manyAssociationType.qualifiedName(), new LinkedHashSet<QualifiedIdentity>() );
                break;
            }
            case MANY:
            {
                manyAssociations.put( manyAssociationType.qualifiedName(), new HashSet<QualifiedIdentity>() );
                break;
            }
            }
        }
        return manyAssociations;
    }

    private EntityStatus status;
    private boolean modified;

    private final long version;
    private final long lastModified;
    private final QualifiedIdentity identity;
    private final EntityType entityType;

    protected final Map<String, Object> properties;
    protected final Map<String, QualifiedIdentity> associations;
    protected final Map<String, Collection<QualifiedIdentity>> manyAssociations;

    public DefaultEntityState( QualifiedIdentity identity, EntityType entityType )
    {
        this( 0, System.currentTimeMillis(), identity, EntityStatus.NEW, entityType, new HashMap<String, Object>(), new HashMap<String, QualifiedIdentity>(), newManyCollections( entityType ) );
    }

    public DefaultEntityState( long version,
                               long lastModified,
                               QualifiedIdentity identity,
                               EntityStatus status,
                               EntityType entityType,
                               Map<String, Object> properties,
                               Map<String, QualifiedIdentity> associations,
                               Map<String, Collection<QualifiedIdentity>> manyAssociations )
    {
        this.version = version;
        this.lastModified = lastModified;
        this.identity = identity;
        this.status = status;
        this.entityType = entityType;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    // EntityState implementation
    public final long version()
    {
        return version;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public QualifiedIdentity qualifiedIdentity()
    {
        return identity;
    }

    public Object getProperty( String qualifiedName )
    {
        return properties.get( qualifiedName );
    }

    public void setProperty( String qualifiedName, Object newValue )
    {
        properties.put( qualifiedName, newValue );
        modified = true;
    }

    public QualifiedIdentity getAssociation( String qualifiedName )
    {
        return associations.get( qualifiedName );
    }

    public void setAssociation( String qualifiedName, QualifiedIdentity newEntity )
    {
        associations.put( qualifiedName, newEntity );
        modified = true;
    }

    public Collection<QualifiedIdentity> getManyAssociation( String qualifiedName )
    {
        Collection<QualifiedIdentity> manyAssociation = manyAssociations.get( qualifiedName );

        if( status == EntityStatus.LOADED )
        {
            if( manyAssociation instanceof List )
            {
                manyAssociation = new ModificationTrackerList( (List<QualifiedIdentity>) manyAssociation );
            }
            else if( manyAssociation instanceof Set )
            {
                manyAssociation = new ModificationTrackerSet( (Set<QualifiedIdentity>) manyAssociation );
            }
            else
            {
                manyAssociation = new ModificationTrackerCollection<Collection<QualifiedIdentity>>( manyAssociation );
            }
        }

        return manyAssociation;
    }

    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    public EntityStatus status()
    {
        return status;
    }

    public EntityType entityType()
    {
        return entityType;
    }

    public Iterable<String> propertyNames()
    {
        return properties.keySet();
    }

    public Iterable<String> associationNames()
    {
        return associations.keySet();
    }

    public Iterable<String> manyAssociationNames()
    {
        return manyAssociations.keySet();
    }

    public boolean isModified()
    {
        return modified;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public Map<String, QualifiedIdentity> getAssociations()
    {
        return associations;
    }

    public Map<String, Collection<QualifiedIdentity>> getManyAssociations()
    {
        return manyAssociations;
    }

    @Override public String toString()
    {
        return identity + "(" + properties.size() + " properties, " + associations.size() + " associations, " + manyAssociations.size() + " many-associations)";
    }

    public void clearModified()
    {
        modified = false;
    }

    public void markAsLoaded()
    {
        status = EntityStatus.LOADED;
    }

    public ValueState newValueState( Map<String, Object> values)
    {
        return new DefaultValueState(values);
    }

    protected class ModificationTrackerCollection<T extends Collection<QualifiedIdentity>>
        implements Collection<QualifiedIdentity>
    {
        protected T collection;

        public ModificationTrackerCollection( T collection )
        {
            this.collection = collection;
        }

        public int size()
        {
            return collection.size();
        }

        public boolean isEmpty()
        {
            return collection.isEmpty();
        }

        public boolean contains( Object o )
        {
            return collection.contains( o );
        }

        public Iterator<QualifiedIdentity> iterator()
        {
            return new Iterator<QualifiedIdentity>()
            {
                Iterator<QualifiedIdentity> iterator = collection.iterator();

                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                public QualifiedIdentity next()
                {
                    return iterator.next();
                }

                public void remove()
                {
                    modified = true;
                    iterator.remove();
                }
            };
        }

        public Object[] toArray()
        {
            return collection.toArray();
        }

        public <T> T[] toArray( T[] ts )
        {
            return collection.toArray( ts );
        }

        public boolean add( QualifiedIdentity qualifiedIdentity )
        {
            boolean added = collection.add( qualifiedIdentity );
            if( added )
            {
                modified = true;
            }
            return added;
        }

        public boolean remove( Object o )
        {
            boolean removed = collection.remove( o );
            if( removed )
            {
                modified = true;
            }
            return removed;
        }

        public boolean containsAll( Collection<?> objects )
        {
            return collection.containsAll( objects );
        }

        public boolean addAll( Collection<? extends QualifiedIdentity> qualifiedIdentities )
        {
            modified = true;
            return collection.addAll( qualifiedIdentities );
        }

        public boolean removeAll( Collection<?> objects )
        {
            modified = true;
            return collection.removeAll( objects );
        }

        public boolean retainAll( Collection<?> objects )
        {
            modified = true;
            return collection.retainAll( objects );
        }

        public void clear()
        {
            modified = true;
            collection.clear();
        }
    }

    protected class ModificationTrackerList
        extends ModificationTrackerCollection<List<QualifiedIdentity>>
        implements List<QualifiedIdentity>
    {
        public ModificationTrackerList( List<QualifiedIdentity> collection )
        {
            super( collection );
        }

        public boolean addAll( int i, Collection<? extends QualifiedIdentity> qualifiedIdentities )
        {
            modified = true;
            return collection.addAll( i, qualifiedIdentities );
        }

        public QualifiedIdentity get( int i )
        {
            return collection.get( i );
        }

        public QualifiedIdentity set( int i, QualifiedIdentity qualifiedIdentity )
        {
            QualifiedIdentity old = collection.set( i, qualifiedIdentity );
            if( old != qualifiedIdentity )
            {
                modified = true;
            }
            return old;
        }

        public void add( int i, QualifiedIdentity qualifiedIdentity )
        {
            modified = true;
            collection.add( i, qualifiedIdentity );
        }

        public QualifiedIdentity remove( int i )
        {
            modified = true;
            return collection.remove( i );
        }

        public int indexOf( Object o )
        {
            return collection.indexOf( o );
        }

        public int lastIndexOf( Object o )
        {
            return collection.lastIndexOf( o );
        }

        public ListIterator<QualifiedIdentity> listIterator()
        {
            final ListIterator<QualifiedIdentity> iterator = collection.listIterator();

            return new ModificationTrackerListIterator( iterator );
        }

        public ListIterator<QualifiedIdentity> listIterator( int i )
        {
            return new ModificationTrackerListIterator( collection.listIterator( i ) );
        }

        public List<QualifiedIdentity> subList( int i, int i1 )
        {
            return new ModificationTrackerList( collection.subList( i, i1 ) );
        }

        private class ModificationTrackerListIterator implements ListIterator<QualifiedIdentity>
        {
            private final ListIterator<QualifiedIdentity> iterator;

            public ModificationTrackerListIterator( ListIterator<QualifiedIdentity> iterator )
            {
                this.iterator = iterator;
            }

            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            public QualifiedIdentity next()
            {
                return iterator.next();
            }

            public boolean hasPrevious()
            {
                return iterator.hasPrevious();
            }

            public QualifiedIdentity previous()
            {
                return iterator.previous();
            }

            public int nextIndex()
            {
                return iterator.nextIndex();
            }

            public int previousIndex()
            {
                return iterator.previousIndex();
            }

            public void remove()
            {
                modified = true;
                iterator.remove();
            }

            public void set( QualifiedIdentity qualifiedIdentity )
            {
                modified = true;
                iterator.set( qualifiedIdentity );
            }

            public void add( QualifiedIdentity qualifiedIdentity )
            {
                modified = true;
                iterator.add( qualifiedIdentity );
            }
        }
    }

    protected class ModificationTrackerSet
        extends ModificationTrackerCollection<Set<QualifiedIdentity>>
        implements Set<QualifiedIdentity>
    {
        public ModificationTrackerSet( Set<QualifiedIdentity> collection )
        {
            super( collection );
        }
    }
}
