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
package org.qi4j.runtime.association;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.functional.Function2;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.spi.entity.NamedAssociationState;

public class NamedAssociationInstance<T>
    extends AbstractAssociationInstance<T>
    implements NamedAssociation<T>
{

    private final NamedAssociationState namedAssociationState;

    public NamedAssociationInstance( AssociationInfo associationInfo,
                                     Function2<EntityReference, Type, Object> associationFunction,
                                     NamedAssociationState namedAssociationState )
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
        checkImmutable();
        checkType( entity );
        ( (ConstraintsCheck) associationInfo ).checkConstraints( entity );
        return namedAssociationState.put( name, getEntityReference( entity ) );
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

}
