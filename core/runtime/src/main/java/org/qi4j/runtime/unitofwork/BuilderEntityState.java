/*
 * Copyright (c) 2009-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2009-2013, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime.unitofwork;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.util.Classes;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.NamedAssociationState;

/**
 * Implementation of EntityState for use through EntityBuilder.
 */
public final class BuilderEntityState
    implements EntityState
{
    private final EntityDescriptor entityType;
    private final EntityReference reference;
    private final Map<QualifiedName, Object> properties = new HashMap<>();
    private final Map<QualifiedName, EntityReference> associations = new HashMap<>();
    private final Map<QualifiedName, ManyAssociationState> manyAssociations = new HashMap<>();
    private final Map<QualifiedName, NamedAssociationState> namedAssociations = new HashMap<>();

    public BuilderEntityState( EntityDescriptor type, EntityReference reference )
    {
        this.entityType = type;
        this.reference = reference;
    }

    @Override
    public EntityReference identity()
    {
        return reference;
    }

    @Override
    public String version()
    {
        return "";
    }

    @Override
    public long lastModified()
    {
        return 0;
    }

    @Override
    public void remove()
    {
    }

    @Override
    public EntityStatus status()
    {
        return EntityStatus.NEW;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return Classes.exactTypeSpecification( type ).satisfiedBy( entityType );
    }

    @Override
    public EntityDescriptor entityDescriptor()
    {
        return entityType;
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        return properties.get( stateName );
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        return associations.get( stateName );
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object newValue )
    {
        properties.put( stateName, newValue );
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        associations.put( stateName, newEntity );
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        ManyAssociationState state = manyAssociations.get( stateName );
        if( state == null )
        {
            state = new BuilderManyAssociationState();
            manyAssociations.put( stateName, state );
        }
        return state;
    }

    @Override
    public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
    {
        NamedAssociationState state = namedAssociations.get( stateName );
        if( state == null )
        {
            state = new BuilderNamedAssociationState();
            namedAssociations.put( stateName, state );
        }
        return state;
    }

    public void copyTo( EntityState newEntityState )
    {
        for( Map.Entry<QualifiedName, Object> fromPropertyEntry : properties.entrySet() )
        {
            newEntityState.setPropertyValue( fromPropertyEntry.getKey(), fromPropertyEntry.getValue() );
        }
        for( Map.Entry<QualifiedName, EntityReference> fromAssociationEntry : associations.entrySet() )
        {
            newEntityState.setAssociationValue( fromAssociationEntry.getKey(), fromAssociationEntry.getValue() );
        }
        for( Map.Entry<QualifiedName, ManyAssociationState> fromManyAssociationEntry : manyAssociations.entrySet() )
        {
            QualifiedName qName = fromManyAssociationEntry.getKey();
            ManyAssociationState fromManyAssoc = fromManyAssociationEntry.getValue();
            ManyAssociationState toManyAssoc = newEntityState.manyAssociationValueOf( qName );
            for( EntityReference entityReference : fromManyAssoc )
            {
                toManyAssoc.add( 0, entityReference );
            }
        }
        for( Map.Entry<QualifiedName, NamedAssociationState> fromNamedAssociationEntry : namedAssociations.entrySet() )
        {
            QualifiedName qName = fromNamedAssociationEntry.getKey();
            NamedAssociationState fromNamedAssoc = fromNamedAssociationEntry.getValue();
            NamedAssociationState toNamedAssoc = newEntityState.namedAssociationValueOf( qName );
            for( String name : fromNamedAssoc )
            {
                toNamedAssoc.put( name, fromNamedAssoc.get( name ) );
            }
        }
    }
}
