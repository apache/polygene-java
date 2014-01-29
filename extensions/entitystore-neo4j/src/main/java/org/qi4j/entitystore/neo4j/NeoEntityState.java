package org.qi4j.entitystore.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.NamedAssociationState;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;

public class NeoEntityState
    implements EntityState
{

    static final String ENTITY_ID = "entity_id";
    static final String VERSION = "version";
    static final String MODIFIED = "modified";
    private final NeoEntityStoreUnitOfWork uow;
    private final ValueSerialization valueSerialization;
    private final Node underlyingNode;
    private EntityStatus status;

    NeoEntityState( ValueSerialization valueSerialization, NeoEntityStoreUnitOfWork work, Node node,
                    EntityStatus status )
    {
        this.valueSerialization = valueSerialization;
        this.uow = work;
        this.underlyingNode = node;
        this.status = status;
    }

    protected void setUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
            Long version = (Long) underlyingNode.getProperty( VERSION );
            underlyingNode.setProperty( VERSION, version + 1 );
            underlyingNode.setProperty( MODIFIED, uow.currentTime() );
        }
    }

    static RelationshipType namedAssociation( QualifiedName stateName )
    {
        return DynamicRelationshipType.withName( "named_association::" + stateName.toString() );
    }

    static RelationshipType manyAssociation( QualifiedName stateName )
    {
        return DynamicRelationshipType.withName( "many_association::" + stateName.toString() );
    }

    static RelationshipType association( QualifiedName stateName )
    {
        return DynamicRelationshipType.withName( "association::" + stateName.toString() );
    }

    @Override
    public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
    {
        RelationshipType namedAssociation = namedAssociation( stateName );
        Relationship rel = underlyingNode.getSingleRelationship( namedAssociation, Direction.OUTGOING );
        if( rel != null )
        {
            return new NeoNamedAssociationState( uow, this, rel.getEndNode() );
        }
        Node node = uow.getNeo().createNode();
        node.setProperty( NeoNamedAssociationState.COUNT, 0 );
        underlyingNode.createRelationshipTo( node, namedAssociation );
        return new NeoNamedAssociationState( uow, this, node );
    }
    
    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        RelationshipType manyAssociation = manyAssociation( stateName );
        Relationship rel = underlyingNode.getSingleRelationship( manyAssociation, Direction.OUTGOING );
        if( rel != null )
        {
            return new NeoManyAssociationState( uow, this, rel.getEndNode() );
        }
        Node node = uow.getNeo().createNode();
        node.setProperty( NeoManyAssociationState.COUNT, 0 );
        underlyingNode.createRelationshipTo( node, manyAssociation );
        return new NeoManyAssociationState( uow, this, node );
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        Relationship rel = underlyingNode.getSingleRelationship( association( stateName ), Direction.OUTGOING );
        if( rel != null )
        {
            String entityId = (String) rel.getEndNode().getProperty( ENTITY_ID );
            return new EntityReference( entityId );
        }
        return null;
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        RelationshipType association = association( stateName );
        Relationship rel = underlyingNode.getSingleRelationship( association, Direction.OUTGOING );
        if( rel != null )
        {
            Node otherNode = rel.getEndNode();
            if( otherNode.getProperty( ENTITY_ID ).equals( identity().identity() ) )
            {
                otherNode.delete();
            }
            rel.delete();
        }
        if( newEntity != null )
        {
            Node otherNode = uow.getEntityStateNode( newEntity );
            if( otherNode.equals( underlyingNode ) )
            {
                // create a blank node for self reference
                otherNode = uow.getNeo().createNode();
                otherNode.setProperty( ENTITY_ID, identity().identity() );
            }
            underlyingNode.createRelationshipTo( otherNode, association );
        }
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        try
        {
            PropertyDescriptor persistentProperty = entityDescriptor().state().findPropertyModelByQualifiedName( stateName );
            Object prop = underlyingNode.getProperty( "prop::" + stateName.toString(), null );
            if( prop == null )
            {
                return null;
            }
            else if( ValueType.isPrimitiveValueType( persistentProperty.valueType() ) )
            {
                return prop;
            }
            else
            {
                return valueSerialization.deserialize( persistentProperty.valueType(), prop.toString() );
            }
        }
        catch( ValueSerializationException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object prop )
    {
        try
        {
            if( prop != null )
            {
                PropertyDescriptor persistentProperty = entityDescriptor().state().findPropertyModelByQualifiedName( stateName );
                if( ValueType.isPrimitiveValueType( persistentProperty.valueType() ) )
                {
                    underlyingNode.setProperty( "prop::" + stateName.toString(), prop );
                }
                else
                {
                    String jsonString = valueSerialization.serialize( prop );
                    underlyingNode.setProperty( "prop::" + stateName.toString(), jsonString );
                }
            }
            else
            {
                underlyingNode.removeProperty( stateName.toString() );
            }
            setUpdated();
        }
        catch( ValueSerializationException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void remove()
    {
        // Apparently remove should just force remove associations instead
        // of throwing exception if the entity has incomming associations
//            if ( underlyingNode.hasRelationship( Direction.INCOMING ) )
//            {
//                throw new IllegalStateException(
//                    "Cannot remove entity with reference: " + identity()
//                    + ". It has incoming associtaions.");
//            }
        // remove of all incomming associations
        for( Relationship rel : underlyingNode.getRelationships( Direction.INCOMING ) )
        {
            rel.delete();
        }
        uow.getIndexService().removeIndex( underlyingNode,
                                           NeoEntityStoreUnitOfWork.ENTITY_STATE_ID,
                                           underlyingNode.getProperty( ENTITY_ID ) );

        for( Relationship rel : underlyingNode.getRelationships( Direction.OUTGOING ) )
        {
            Node endNode = rel.getEndNode();
            boolean manyAssocNode = false, namedAssocNode = false;
            for( Relationship manyRel : endNode.getRelationships( RelTypes.MANY_ASSOCIATION, Direction.OUTGOING ) )
            {
                manyRel.delete();
                manyAssocNode = true;
            }
            for( Relationship namedRel : endNode.getRelationships( RelTypes.NAMED_ASSOCIATION, Direction.OUTGOING ) )
            {
                namedRel.delete();
                namedAssocNode = true;
            }
            if( manyAssocNode || namedAssocNode )
            {
                endNode.delete();
            }
            rel.delete();
        }
        underlyingNode.delete();
        status = EntityStatus.REMOVED;
    }

    @Override
    public EntityDescriptor entityDescriptor()
    {
        Node typeNode = underlyingNode.getSingleRelationship( RelTypes.IS_OF_TYPE, Direction.OUTGOING ).getEndNode();
        String type = (String) typeNode.getProperty( NeoEntityStoreUnitOfWork.ENTITY_TYPE );
        return uow.getEntityDescriptor( type );
    }

    public void hasBeenApplied()
    {
        // TODO
    }

    @Override
    public EntityReference identity()
    {
        return new EntityReference( (String) underlyingNode.getProperty( ENTITY_ID ) );
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        Node typeNode = underlyingNode.getSingleRelationship( RelTypes.IS_OF_TYPE, Direction.OUTGOING ).getEndNode();
        String typeName = (String) typeNode.getProperty( NeoEntityStoreUnitOfWork.ENTITY_TYPE );
        return typeName.equals( type.getName() );
    }

    @Override
    public long lastModified()
    {
        long modified = (Long) underlyingNode.getProperty( MODIFIED );
        return modified;
    }

    @Override
    public EntityStatus status()
    {
        return status;
    }

    @Override
    public String version()
    {
        long version = (Long) underlyingNode.getProperty( VERSION );
        if( status == EntityStatus.UPDATED )
        {
            version--;
        }
        return "" + version;
    }

    public EntityStoreUnitOfWork unitOfWork()
    {
        return uow;
    }
}
