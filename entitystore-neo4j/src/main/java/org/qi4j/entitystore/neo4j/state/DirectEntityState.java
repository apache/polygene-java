/* Copyright 2008 Neo Technology, http://neotechnology.com.
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
package org.qi4j.entitystore.neo4j.state;

import org.neo4j.api.core.*;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.helpers.DefaultValueState;
import org.qi4j.spi.value.ValueState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectEntityState implements CommittableEntityState
{
    // Constants
    public static final String IDENTITY_PROPERTY_KEY = "<entity identity>";
    public static final String TYPE_PROPERTY_KEY = "<entity type>";
    public static final String ASSOCIATION_OF_PROPERTY_KEY = "<association of>";
    public static final RelationshipType PROXY_FOR = LinkType.UNQUALIFIED.getRelationshipType( "<PROXY FOR>" );
    private static final String VERSION_PROPERTY_KEY = "<entity version>";
    private static final String LASTMODIFIED_PROPERTY_KEY = "<last modified>";
    private static String COLLECTION_SIZE_PROPERTY_PREFIX = "size of::";
    private static final RelationshipType ENTITY_TYPE_RELATIONSHIP_TYPE = LinkType.UNQUALIFIED.getRelationshipType( "<ENTITY TYPE>" );

    // Internal state
    public final Node underlyingNode;
    private final NeoIdentityIndex idIndex;
    private final QualifiedIdentity identity;
    private EntityStatus status;
    private EntityType entityType;
    private final LoadedDescriptor descriptor;
    private final Map<QualifiedName, Collection<QualifiedIdentity>> manyAssociations = new HashMap<QualifiedName, Collection<QualifiedIdentity>>();
    private boolean loaded = false;
    final NeoService neo;

    public DirectEntityState( NeoService neo, NeoIdentityIndex idIndex, Node underlyingNode, QualifiedIdentity identity, EntityStatus status, LoadedDescriptor descriptor )
    {
        this.neo = neo;
        this.idIndex = idIndex;
        this.underlyingNode = underlyingNode;
        this.identity = identity;
        this.status = status;
        this.entityType = null; // TODO Generate from descriptor?
        this.descriptor = descriptor;
        populateManyAssociations( neo );
    }

    private void populateManyAssociations( NeoService neo )
    {
        for( ManyAssociationFactory factory : getManyAssociationFactories() )
        {
            manyAssociations.put( factory.getQualifiedName(), factory.createNodeCollection( this, neo, idIndex ) );
        }
    }

    // CommittableEntityState implementation

    public void preloadState()
    {
        if( !loaded )
        {
            loaded = true;
            if( status == EntityStatus.NEW )
            {
                storeIdentity();
            }
            else
            {
                verifyIdentity();
            }
        }
    }

    public void prepareState()
    {
        // Only used by IndirectEntityState
    }

    public void prepareCommit()
    {
        underlyingNode.setProperty( VERSION_PROPERTY_KEY, version() + 1 );
        underlyingNode.setProperty( LASTMODIFIED_PROPERTY_KEY, System.currentTimeMillis() );
    }

    private void storeIdentity()
    {
        Node typeNode = idIndex.getTypeNode( identity.type() );
        underlyingNode.setProperty( IDENTITY_PROPERTY_KEY, identity.identity() );
        underlyingNode.createRelationshipTo( typeNode, ENTITY_TYPE_RELATIONSHIP_TYPE );
        idIndex.putNode( identity.identity(), underlyingNode );
    }

    private void verifyIdentity()
    {
        String errorMessage = "Stored identity does not match expected identity. ";
        Relationship typeRelation = underlyingNode.getSingleRelationship( ENTITY_TYPE_RELATIONSHIP_TYPE, Direction.OUTGOING );
        Node expectedTypeNode = idIndex.getTypeNode( identity.type() );
        if( typeRelation == null )
        {
            throw new IllegalStateException( errorMessage + "Not related to a type." );
        }
        else if( !expectedTypeNode.equals( typeRelation.getEndNode() ) )
        {
            throw new IllegalStateException( errorMessage + "Related to wrong type." );
        }
        else if( !identity.identity().equals( underlyingNode.getProperty( IDENTITY_PROPERTY_KEY, null ) ) )
        {
            throw new IllegalStateException( errorMessage + "Expected id \"" + identity.identity() +
                                             "\", was \"" + underlyingNode.getProperty( IDENTITY_PROPERTY_KEY, "null" ) + "\"." );
        }
    }

    public Iterable<ManyAssociationFactory> getManyAssociationFactories()
    {
        return descriptor.getManyAssociationFactories();
    }

    public boolean isUpdated()
    {
        return true; // TODO: implement some logic here.
    }

    public void prepareRemove()
    {
        for( Collection<QualifiedIdentity> manyAssociation : manyAssociations.values() )
        {
            manyAssociation.clear();
        }
        for( Relationship relation : underlyingNode.getRelationships() )
        {
            if( relation.getStartNode().equals( underlyingNode ) || LinkType.END.isInstance( relation ) )
            {
                relation.delete();
            }
        }
    }

    // EntityState implementation

    public void remove()
    {
        for( Relationship relation : underlyingNode.getRelationships( Direction.INCOMING ) )
        {
            if( !LinkType.END.isInstance( relation ) )
            {
                throw new IllegalStateException( "Cannot remove entity with identity: " + identity
                                                 + ". It has incoming associtaions." );
            }
        }
        prepareRemove();
        underlyingNode.delete();
        status = EntityStatus.REMOVED;
    }

    public QualifiedIdentity qualifiedIdentity()
    {
        return identity;
    }

    public long version()
    {
        return (Long) underlyingNode.getProperty( VERSION_PROPERTY_KEY, 0L );
    }

    public long lastModified()
    {
        return (Long) underlyingNode.getProperty( LASTMODIFIED_PROPERTY_KEY, System.currentTimeMillis() );
    }

    public EntityStatus status()
    {
        return status;
    }

    public EntityType entityType()
    {
        return entityType;
    }

    public Iterable<QualifiedName> propertyNames()
    {
        return descriptor.getPropertyNames();
    }

    public Iterable<QualifiedName> associationNames()
    {
        return descriptor.getAssociationNames();
    }

    public Iterable<QualifiedName> manyAssociationNames()
    {
        return descriptor.getManyAssociationNames();
    }

    public Object getProperty( QualifiedName qualifiedName )
    {
        return underlyingNode.getProperty( qualifiedName.toString(), null );
    }

    public void setProperty( QualifiedName qualifiedName, Object newValue )
    {
        if( newValue != null )
        {
            underlyingNode.setProperty( qualifiedName.toString(), newValue );
        }
        else if( underlyingNode.hasProperty( qualifiedName.toString() ) )
        {
            underlyingNode.removeProperty( qualifiedName.toString() );
        }
    }

    public QualifiedIdentity getAssociation( QualifiedName qualifiedName )
    {
        RelationshipType associationType = getAssociationType( qualifiedName.name() );
        Relationship relation = underlyingNode.getSingleRelationship( associationType, Direction.OUTGOING );
        if( relation != null )
        {
            return getIdentityFromNode( unproxy( relation.getEndNode() ) );
        }
        else
        {
            return null;
        }
    }

    public void setAssociation( QualifiedName qualifiedName, QualifiedIdentity newEntity )
    {
        RelationshipType associationType = getAssociationType( qualifiedName.name() );
        Relationship relation = underlyingNode.getSingleRelationship( associationType, Direction.OUTGOING );
        if( relation != null )
        {
            removeProxy( relation.getEndNode() );
            relation.delete();
        }
        if( newEntity != null )
        {
            Node otherNode = idIndex.getNode( newEntity.identity() );
            if( underlyingNode.equals( otherNode ) )
            {
                otherNode = proxy( neo, otherNode );
            }
            underlyingNode.createRelationshipTo( otherNode, associationType );
        }
    }

    public Collection<QualifiedIdentity> getManyAssociation( QualifiedName qualifiedName )
    {
        return manyAssociations.get( qualifiedName );
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
    }

    public ValueState newValueState( Map<QualifiedName, Object> values )
    {
        // TODO Replace with something Neo4j specific!
        return new DefaultValueState( values );
    }

    // Implementation internals

    private RelationshipType getAssociationType( String qualifiedName )
    {
        return LinkType.UNQUALIFIED.getRelationshipType( qualifiedName );
    }

    static QualifiedIdentity getIdentityFromNode( Node node )
    {
        String id = (String) node.getProperty( IDENTITY_PROPERTY_KEY );
        Node typeNode = node.getSingleRelationship( ENTITY_TYPE_RELATIONSHIP_TYPE, Direction.OUTGOING ).getEndNode();
        String clazz = (String) typeNode.getProperty( TYPE_PROPERTY_KEY );
        return new QualifiedIdentity( id, clazz );
    }

    public int getSizeOfCollection( QualifiedName qualifiedName )
    {
        return (Integer) underlyingNode.getProperty( COLLECTION_SIZE_PROPERTY_PREFIX + qualifiedName, 0 );
    }

    public void setSizeOfCollection( QualifiedName qualifiedName, int size )
    {
        underlyingNode.setProperty( COLLECTION_SIZE_PROPERTY_PREFIX + qualifiedName, size );
    }

    public static Node proxy( NeoService neo, Node original )
    {
        Node proxy = neo.createNode();
        proxy.createRelationshipTo( original, DirectEntityState.PROXY_FOR );
        return proxy;
    }

    public static Node unproxy( Node listed )
    {
        if( listed == null )
        {
            return null;
        }
        Relationship proxyRelation = listed.getSingleRelationship( DirectEntityState.PROXY_FOR, Direction.OUTGOING );
        if( proxyRelation != null )
        {
            return proxyRelation.getEndNode();
        }
        else
        {
            return listed;
        }
    }

    static void removeProxy( Node listed )
    {
        if( listed == null )
        {
            return;
        }
        Relationship proxyRelation = listed.getSingleRelationship( DirectEntityState.PROXY_FOR, Direction.OUTGOING );
        if( proxyRelation != null )
        {
            proxyRelation.delete();
            listed.delete();
        }
    }
}
