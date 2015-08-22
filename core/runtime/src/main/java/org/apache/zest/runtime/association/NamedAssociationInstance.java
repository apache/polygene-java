/*
 * Copyright (c) 2011-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.runtime.association;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.association.NamedAssociationWrapper;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.spi.entity.NamedAssociationState;

import static org.apache.zest.functional.Iterables.map;

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
        return namedAssociationState.put( name, new EntityReference( ( (Identity) entity ).identity().get() ) );
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
    public Iterable<EntityReference> references()
    {
        return map( new Function<String, EntityReference>()
        {
            @Override
            public EntityReference apply( String name )
            {
                return namedAssociationState.get( name );
            }
        }, namedAssociationState );
    }

    @Override
    public EntityReference referenceOf( String name )
    {
        return namedAssociationState.get( name );
    }

    public Iterable<Map.Entry<String, EntityReference>> getEntityReferences()
    {
        return map( new Function<String, Map.Entry<String, EntityReference>>()
        {
            @Override
            public Map.Entry<String, EntityReference> apply( final String key )
            {
                final EntityReference value = namedAssociationState.get( key );
                return new Map.Entry<String, EntityReference>()
                {
                    @Override
                    public String getKey()
                    {
                        return key;
                    }

                    @Override
                    public EntityReference getValue()
                    {
                        return value;
                    }

                    @Override
                    public EntityReference setValue( EntityReference value )
                    {
                        throw new UnsupportedOperationException( "Immutable Map" );
                    }

                    @Override
                    public boolean equals( Object o )
                    {
                        if( o instanceof Map.Entry )
                        {
                            Map.Entry other = (Map.Entry) o;
                            return key.equals( other.getKey() );
                        }
                        return false;
                    }

                    @Override
                    public int hashCode()
                    {
                        return 997 * key.hashCode() + 981813497;
                    }
                };
            }
        }, namedAssociationState );
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
