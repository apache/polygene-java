package org.qi4j.entitystore.neo4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;

public class NeoManyAssociationState
    implements ManyAssociationState
{
    private static final String INDEX = "index";
    static final String COUNT = "count";

    private final Node underlyingNode;
    private final NeoEntityState entity;
    private final NeoEntityStoreUnitOfWork uow;

    NeoManyAssociationState( NeoEntityStoreUnitOfWork uow,
                             NeoEntityState entity, Node node
    )
    {
        this.uow = uow;
        this.entity = entity;
        this.underlyingNode = node;
    }

    public boolean add( int index, EntityReference entityReference )
    {
        if( index < 0 || index > count() )
        {
            throw new IllegalArgumentException( "Illegal index: " + index );
        }
        Node entityNode = uow.getEntityStateNode( entityReference );
        for( Relationship rel : underlyingNode.getRelationships(
            RelTypes.MANY_ASSOCIATION, Direction.OUTGOING ) )
        {
            int relIndex = getRelationshipIndex( rel );
            if( relIndex >= index )
            {
                setRelationshipIndex( rel, relIndex + 1 );
            }
            if( rel.getEndNode().equals( entityNode ) )
            {
                return false;
            }
        }
        Relationship rel = underlyingNode.createRelationshipTo( entityNode,
                                                                RelTypes.MANY_ASSOCIATION );
        setRelationshipIndex( rel, index );
        incrementCount();
        entity.setUpdated();
        return true;
    }

    private int getRelationshipIndex( Relationship rel )
    {
        return (Integer) rel.getProperty( INDEX );
    }

    private void setRelationshipIndex( Relationship rel, int newIndex )
    {
        rel.setProperty( INDEX, newIndex );
    }

    private void incrementCount()
    {
        int count = (Integer) underlyingNode.getProperty( "count" );
        underlyingNode.setProperty( COUNT, count + 1 );
    }

    private void decrementCount()
    {
        int count = (Integer) underlyingNode.getProperty( "count" );
        underlyingNode.setProperty( COUNT, --count );
    }

    public boolean contains( EntityReference entityReference )
    {
        Node entityNode = uow.getEntityStateNode( entityReference );
        for( Relationship rel : underlyingNode.getRelationships(
            RelTypes.MANY_ASSOCIATION, Direction.OUTGOING ) )
        {
            if( rel.getEndNode().equals( entityNode ) )
            {
                return true;
            }
        }
        return false;
    }

    public int count()
    {
        return (Integer) underlyingNode.getProperty( COUNT );
    }

    public EntityReference get( int index )
    {
        if( index < 0 || index > count() )
        {
            throw new IllegalArgumentException( "Illegal index: " + index );
        }
        for( Relationship rel : underlyingNode.getRelationships(
            RelTypes.MANY_ASSOCIATION, Direction.OUTGOING ) )
        {
            int relIndex = getRelationshipIndex( rel );
            if( relIndex == index )
            {
                String id = (String) rel.getEndNode().getProperty(
                    NeoEntityState.ENTITY_ID );
                return new EntityReference( id );
            }
        }
        return null;
    }

    public boolean remove( EntityReference entityReference )
    {
        Node entityNode = uow.getEntityStateNode( entityReference );
        int indexDeleted = -1;
        for( Relationship rel : entityNode.getRelationships(
            RelTypes.MANY_ASSOCIATION, Direction.INCOMING ) )
        {
            if( rel.getStartNode().equals( underlyingNode ) )
            {
                indexDeleted = getRelationshipIndex( rel );
                rel.delete();
                break;
            }
        }
        if( indexDeleted == -1 )
        {
            return false;
        }
        for( Relationship rel : underlyingNode.getRelationships(
            RelTypes.MANY_ASSOCIATION, Direction.OUTGOING ) )
        {
            int relIndex = getRelationshipIndex( rel );
            if( relIndex > indexDeleted )
            {
                setRelationshipIndex( rel, relIndex - 1 );
            }
        }
        decrementCount();
        entity.setUpdated();
        return true;
    }

    public Iterator<EntityReference> iterator()
    {
        List<EntityReference> list = new ArrayList<EntityReference>( count() );
        for( Relationship rel : underlyingNode.getRelationships(
            RelTypes.MANY_ASSOCIATION, Direction.OUTGOING ) )
        {
            String id = (String) rel.getEndNode().getProperty(
                NeoEntityState.ENTITY_ID );
            list.add( new EntityReference( id ) );
        }
        return list.iterator();
    }
}