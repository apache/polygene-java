/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.serialization;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.StateName;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serializable state for a single entity. This includes the version
 * of the state and the version of the type.
 */
public final class SerializableState
    implements Externalizable
{
    private static final long serialVersionUID = 5L;

    private EntityReference identity;
    private String entityVersion;
    private long lastModified;
    private Set<EntityTypeReference> entityTypeReferences;
    private Map<StateName, String> properties;
    private Map<StateName, EntityReference> associations;
    private Map<StateName, List<EntityReference>> manyAssociations;

    public SerializableState()
    {
        // Externalizable constructor
    }

    public SerializableState( EntityReference identity,
                              String entityVersion, long lastModified,
                              Set<EntityTypeReference> entityTypeReferences,
                              Map<StateName, String> properties,
                              Map<StateName, EntityReference> associations,
                              Map<StateName, List<EntityReference>> manyAssociations )
    {
        this.identity = identity;
        this.entityVersion = entityVersion;
        this.lastModified = lastModified;
        this.entityTypeReferences = entityTypeReferences;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    public EntityReference identity()
    {
        return identity;
    }

    public Set<EntityTypeReference> entityTypeReferences()
    {
        return entityTypeReferences;
    }

    public String version()
    {
        return entityVersion;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public Map<StateName, String> properties()
    {
        return properties;
    }

    public Map<StateName, EntityReference> associations()
    {
        return associations;
    }

    public Map<StateName, List<EntityReference>> manyAssociations()
    {
        return manyAssociations;
    }

    public void addEntityTypeReference( EntityTypeReference addedTypeReference, String version, long lastModified )
    {
        entityTypeReferences.add( addedTypeReference );
        entityVersion = version;
        this.lastModified = lastModified;
    }

    public void removeEntityTypeReference( EntityTypeReference addedTypeReference, String version, long lastModified )
    {
        entityTypeReferences.remove( addedTypeReference );
        entityVersion = version;
        this.lastModified = lastModified;
    }

    public void setProperty( StateName stateName, String value, String version, long lastModified )
    {
        properties.put( stateName, value );
        entityVersion = version;
        this.lastModified = lastModified;
    }

    public void setAssociation( StateName stateName, EntityReference associatedEntity, String version, long lastModified )
    {
        associations.put( stateName, associatedEntity );
        entityVersion = version;
        this.lastModified = lastModified;
    }

    public void addManyAssociation( StateName stateName, int index, EntityReference associatedEntity, String version, long lastModified )
    {
        List<EntityReference> manyAssociationState = manyAssociations.get( stateName );
        if( manyAssociationState == null )
        {
            manyAssociationState = new ArrayList<EntityReference>();
            manyAssociations.put( stateName, manyAssociationState );
        }
        manyAssociationState.add( index, associatedEntity );

        entityVersion = version;
        this.lastModified = lastModified;
    }

    public void removeManyAssociation( StateName stateName, EntityReference associatedEntity, String version, long lastModified )
    {
        List<EntityReference> manyAssociationState = manyAssociations.get( stateName );
        if( manyAssociationState == null )
        {
            return;
        }
        manyAssociationState.remove( associatedEntity );

        entityVersion = version;
        this.lastModified = lastModified;
    }

    public void writeExternal( ObjectOutput out ) throws IOException
    {
        out.writeUTF( identity.identity() );
        out.writeUTF( entityVersion );
        out.writeLong( lastModified );

        out.writeInt( entityTypeReferences.size() );
        for( EntityTypeReference entityTypeReference : entityTypeReferences )
        {
            out.writeUTF( entityTypeReference.toString() );
        }

        out.writeInt( properties.size() );
        for( Map.Entry<StateName, String> stateNameStringEntry : properties.entrySet() )
        {
            out.writeUTF( stateNameStringEntry.getKey().toString() );
            out.writeUTF( stateNameStringEntry.getValue() );
        }

        out.writeInt( associations.size() );
        for( Map.Entry<StateName, EntityReference> stateNameEntityReferenceEntry : associations.entrySet() )
        {
            out.writeUTF( stateNameEntityReferenceEntry.getKey().toString() );
            EntityReference value = stateNameEntityReferenceEntry.getValue();
            if( value == null )
            {
                out.writeUTF( "null" );
            }
            else
            {
                out.writeUTF( value.identity() );
            }
        }

        out.writeInt( manyAssociations.size() );
        for( Map.Entry<StateName, List<EntityReference>> stateNameListEntry : manyAssociations.entrySet() )
        {
            out.writeUTF( stateNameListEntry.getKey().toString() );
            List<EntityReference> list = stateNameListEntry.getValue();
            out.writeInt( list.size() );
            for( EntityReference entityReference : list )
            {
                out.writeUTF( entityReference.identity() );
            }
        }
    }

    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
    {
        String identityStr = in.readUTF();
        identity = EntityReference.parseEntityReference( identityStr );
        entityVersion = in.readUTF();
        lastModified = in.readLong();

        int size = in.readInt();
        entityTypeReferences = new HashSet<EntityTypeReference>( size );
        for( int i = 0; i < size; i++ )
        {
            String typeStr = in.readUTF();
            entityTypeReferences.add( new EntityTypeReference( typeStr ) );
        }

        size = in.readInt();
        properties = new HashMap<StateName, String>();
        for( int i = 0; i < size; i++ )
        {
            String stateNameStr = in.readUTF();
            String propertyValueStr = in.readUTF();
            properties.put( new StateName( stateNameStr ), propertyValueStr );
        }

        size = in.readInt();
        associations = new HashMap<StateName, EntityReference>();
        for( int i = 0; i < size; i++ )
        {
            String stateNameStr = in.readUTF();
            String refStr = in.readUTF();
            if( !refStr.equals( "null" ) )
            {
                associations.put( new StateName( stateNameStr ), EntityReference.parseEntityReference( refStr ) );
            }
            else
            {
                associations.put( new StateName( stateNameStr ), null );
            }
        }

        size = in.readInt();
        manyAssociations = new HashMap<StateName, List<EntityReference>>();
        for( int i = 0; i < size; i++ )
        {
            String stateNameStr = in.readUTF();
            StateName stateName = new StateName( stateNameStr );
            int listSize = in.readInt();
            List<EntityReference> list = new ArrayList<EntityReference>( listSize );
            for( int j = 0; j < listSize; j++ )
            {
                String refStr = in.readUTF();
                list.add( EntityReference.parseEntityReference( refStr ) );
            }
            manyAssociations.put( stateName, list );
        }
    }
}
