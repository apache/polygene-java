package org.qi4j.entitystore.neo4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.NamedAssociationState;

public class NeoNamedAssociationState
    implements NamedAssociationState
{
    private static final String NAME = "name";
    static final String COUNT = "count";

    private final Node underlyingNode;
    private final NeoEntityState entity;
    private final NeoEntityStoreUnitOfWork uow;

    NeoNamedAssociationState( NeoEntityStoreUnitOfWork uow,
                              NeoEntityState entity, Node node
    )
    {
        this.uow = uow;
        this.entity = entity;
        this.underlyingNode = node;
    }

    @Override
    public boolean put( String name, EntityReference entityReference )
    {
        Node entityNode = uow.getEntityStateNode( entityReference );
        for( Relationship rel : underlyingNode.getRelationships( RelTypes.NAMED_ASSOCIATION, Direction.OUTGOING ) )
        {
            String relName = getRelationshipName( rel );
            if( relName.equals( name ) )
            {
                if( rel.getEndNode().equals( entityNode ) )
                {
                    return false;
                }
                rel.delete();
                decrementCount();
                break;
            }
        }
        Relationship rel = underlyingNode.createRelationshipTo( entityNode, RelTypes.NAMED_ASSOCIATION );
        setRelationshipName( rel, name );
        incrementCount();
        entity.setUpdated();
        return true;
    }

    private String getRelationshipName( Relationship rel )
    {
        return (String) rel.getProperty( NAME );
    }

    private void setRelationshipName( Relationship rel, String newName )
    {
        rel.setProperty( NAME, newName );
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

    @Override
    public boolean containsName( String name )
    {
        for( Relationship rel : underlyingNode.getRelationships( RelTypes.NAMED_ASSOCIATION, Direction.OUTGOING ) )
        {
            if( getRelationshipName( rel ).equals( name ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int count()
    {
        return (Integer) underlyingNode.getProperty( COUNT );
    }

    @Override
    public EntityReference get( String name )
    {
        for( Relationship rel : underlyingNode.getRelationships( RelTypes.NAMED_ASSOCIATION, Direction.OUTGOING ) )
        {
            if( getRelationshipName( rel ).equals( name ) )
            {
                String id = (String) rel.getEndNode().getProperty( NeoEntityState.ENTITY_ID );
                return new EntityReference( id );
            }
        }
        return null;
    }

    @Override
    public boolean remove( String name )
    {
        for( Relationship rel : underlyingNode.getRelationships( RelTypes.NAMED_ASSOCIATION, Direction.OUTGOING ) )
        {
            if( getRelationshipName( rel ).equals( name ) )
            {
                rel.delete();
                decrementCount();
                entity.setUpdated();
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<String> iterator()
    {
        List<String> list = new ArrayList<>();
        for( Relationship rel : underlyingNode.getRelationships( RelTypes.NAMED_ASSOCIATION, Direction.OUTGOING ) )
        {
            list.add( getRelationshipName( rel ) );
        }
        return list.iterator();
    }

    @Override
    public String nameOf( EntityReference entityReference )
    {
        Node entityNode = uow.getEntityStateNode( entityReference );
        for( Relationship rel : underlyingNode.getRelationships( RelTypes.NAMED_ASSOCIATION, Direction.OUTGOING ) )
        {
            if( rel.getEndNode().equals( entityNode ) )
            {
                return getRelationshipName( rel );
            }
        }
        return null;
    }

}
