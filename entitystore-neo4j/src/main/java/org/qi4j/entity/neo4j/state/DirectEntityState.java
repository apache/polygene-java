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
package org.qi4j.entity.neo4j.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.qi4j.entity.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;

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
    private static String COLLECTION_SIZE_PROPERTY_PREFIX = "size of::";
    private static final RelationshipType ENTITY_TYPE_RELATIONSHIP_TYPE = LinkType.UNQUALIFIED.getRelationshipType( "<ENTITY TYPE>" );

    // Internal state
    public final Node underlyingNode;
    private final NeoIdentityIndex idIndex;
    private final QualifiedIdentity identity;
    private EntityStatus status;
    private final LoadedDescriptor descriptor;
    private final Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
    private boolean loaded = false;

    public DirectEntityState( NeoService neo, NeoIdentityIndex idIndex, Node underlyingNode, QualifiedIdentity identity, EntityStatus status, LoadedDescriptor descriptor )
    {
        this.idIndex = idIndex;
        this.underlyingNode = underlyingNode;
        this.identity = identity;
        this.status = status;
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
        underlyingNode.setProperty( VERSION_PROPERTY_KEY, getEntityVersion() + 1 );
    }

    private void storeIdentity()
    {
        Node typeNode = idIndex.getTypeNode( identity.getCompositeType() );
        underlyingNode.setProperty( IDENTITY_PROPERTY_KEY, identity.getIdentity() );
        underlyingNode.createRelationshipTo( typeNode, ENTITY_TYPE_RELATIONSHIP_TYPE );
        idIndex.putNode( identity.getIdentity(), underlyingNode );
    }

    private void verifyIdentity()
    {
        String errorMessage = "Stored identity does not match expected identity. ";
        Relationship typeRelation = underlyingNode.getSingleRelationship( ENTITY_TYPE_RELATIONSHIP_TYPE, Direction.OUTGOING );
        Node expectedTypeNode = idIndex.getTypeNode( identity.getCompositeType() );
        if( typeRelation == null )
        {
            throw new IllegalStateException( errorMessage + "Not related to a type." );
        }
        else if( !expectedTypeNode.equals( typeRelation.getEndNode() ) )
        {
            throw new IllegalStateException( errorMessage + "Related to wrong type." );
        }
        else if( !identity.getIdentity().equals( underlyingNode.getProperty( IDENTITY_PROPERTY_KEY, null ) ) )
        {
            throw new IllegalStateException( errorMessage + "Expected id \"" + identity.getIdentity() +
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

    public QualifiedIdentity getIdentity()
    {
        return identity;
    }

    public long getEntityVersion()
    {
        return (Long) underlyingNode.getProperty( VERSION_PROPERTY_KEY, 0L );
    }

    public EntityStatus getStatus()
    {
        return status;
    }

    public Iterable<String> getPropertyNames()
    {
        return descriptor.getPropertyNames();
    }

    public Iterable<String> getAssociationNames()
    {
        return descriptor.getAssociationNames();
    }

    public Iterable<String> getManyAssociationNames()
    {
        return descriptor.getManyAssociationNames();
    }

    public Object getProperty( String qualifiedName )
    {
        return underlyingNode.getProperty( qualifiedName, null );
    }

    public void setProperty( String qualifiedName, Object newValue )
    {
        if( newValue != null )
        {
            underlyingNode.setProperty( qualifiedName, newValue );
        }
        else if( underlyingNode.hasProperty( qualifiedName ) )
        {
            underlyingNode.removeProperty( qualifiedName );
        }
    }

    public QualifiedIdentity getAssociation( String qualifiedName )
    {
        RelationshipType associationType = getAssociationType( qualifiedName );
        Relationship relation = underlyingNode.getSingleRelationship( associationType, Direction.OUTGOING );
        if( relation != null )
        {
            return getIdentityFromNode( relation.getEndNode() );
        }
        else
        {
            return null;
        }
    }

    public void setAssociation( String qualifiedName, QualifiedIdentity newEntity )
    {
        RelationshipType associationType = getAssociationType( qualifiedName );
        Relationship relation = underlyingNode.getSingleRelationship( associationType, Direction.OUTGOING );
        if( relation != null )
        {
            relation.delete();
        }
        underlyingNode.createRelationshipTo( idIndex.getNode( newEntity.getIdentity() ), associationType );
    }

    public Collection<QualifiedIdentity> getManyAssociation( String qualifiedName )
    {
        return manyAssociations.get( qualifiedName );
    }

    public Collection<QualifiedIdentity> setManyAssociation( String qualifiedName, Collection<QualifiedIdentity> newManyAssociation )
    {
        throw new UnsupportedOperationException();
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

    public int getSizeOfCollection( String qualifiedName )
    {
        return (Integer) underlyingNode.getProperty( COLLECTION_SIZE_PROPERTY_PREFIX + qualifiedName, 0 );
    }

    public void setSizeOfCollection( String qualifiedName, int size )
    {
        underlyingNode.setProperty( COLLECTION_SIZE_PROPERTY_PREFIX + qualifiedName, size );
    }
}
