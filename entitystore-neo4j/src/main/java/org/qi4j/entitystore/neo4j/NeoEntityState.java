package org.qi4j.entitystore.neo4j;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.property.PropertyType;

public class NeoEntityState
    implements EntityState
{
    static final String ENTITY_ID = "entity_id";
    static final String VERSION = "version";
    static final String MODIFIED = "modified";

    private final Node underlyingNode;
    private final NeoEntityStoreUnitOfWork uow;

    private EntityStatus status;

    NeoEntityState( NeoEntityStoreUnitOfWork work, Node node,
                    EntityStatus status
    )
    {
        this.underlyingNode = node;
        this.uow = work;
        this.status = status;
    }

    protected void setUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
            Long version = (Long) underlyingNode.getProperty( VERSION );
            underlyingNode.setProperty( VERSION, version + 1 );
            underlyingNode.setProperty( MODIFIED, System.currentTimeMillis() );
        }
    }

    static RelationshipType manyAssociation( QualifiedName stateName )
    {
        return DynamicRelationshipType.withName( "many_association::" + stateName.toString() );
    }

    static RelationshipType association( QualifiedName stateName )
    {
        return DynamicRelationshipType.withName( "association::" + stateName.toString() );
    }

    public ManyAssociationState getManyAssociation( QualifiedName stateName )
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

    public EntityReference getAssociation( QualifiedName stateName )
    {
        Relationship rel = underlyingNode.getSingleRelationship( association( stateName ), Direction.OUTGOING );
        if( rel != null )
        {
            String entityId = (String) rel.getEndNode().getProperty( ENTITY_ID );
            return new EntityReference( entityId );
        }
        return null;
    }

    public void setAssociation( QualifiedName stateName, EntityReference newEntity )
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

    public Object getProperty( QualifiedName stateName )
    {
        try
        {
            Object prop = underlyingNode.getProperty( "prop::" + stateName.toString(), null );
            if( prop == null )
            {
                return null;
            }
            else if( isPrimitiveType( prop ) )
            {
                return prop;
            }
            else
            {
                // why is it a set and not a Map?
                for( PropertyType propertyType : entityDescriptor().entityType().properties() )
                {
                    if( propertyType.qualifiedName().equals( stateName ) )
                    {
                        String json = "[" + prop + "]";
                        JSONTokener tokener = new JSONTokener( json );
                        JSONArray array = (JSONArray) tokener.nextValue();
                        Object jsonValue = array.get( 0 );
                        if( jsonValue == JSONObject.NULL )
                        {
                            return null;
                        }
                        else
                        {
                            return propertyType.type().fromJSON( jsonValue, uow.getModule() );
                        }
                    }
                }
            }
            return underlyingNode.getProperty( "prop::" + stateName.toString() ).toString();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public void setProperty( QualifiedName stateName, Object prop )
    {
        try
        {
            if( prop != null )
            {
                if( isPrimitiveType( prop ) )
                {
                    underlyingNode.setProperty( "prop::" + stateName.toString(), prop );
                }
                else
                {
                    // why is it a set and not a Map?
                    for( PropertyType propertyType : entityDescriptor().entityType().properties() )
                    {
                        if( propertyType.qualifiedName().equals( stateName ) )
                        {
                            if( prop instanceof String && propertyType.type().isString() )
                            {
                                underlyingNode.setProperty( "prop::" + stateName.toString(), prop );
                            }
                            else
                            {
                                JSONStringer json = new JSONStringer();
                                json.array();
                                propertyType.type().toJSON( prop, json );
                                json.endArray();
                                String jsonString = json.toString();
                                jsonString = jsonString.substring( 1, jsonString.length() - 1 );
                                underlyingNode.setProperty( "prop::" + stateName.toString(), jsonString );
                            }
                            break;
                        }
                    }
                }
            }
            else
            {
                underlyingNode.removeProperty( stateName.toString() );
            }
            setUpdated();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    private boolean isPrimitiveType( Object prop )
    {
        if( prop instanceof Number || prop instanceof Character || prop instanceof Boolean )
        {
            return true;
        }
        if( prop.getClass().isArray() )
        {
            return isPrimitiveArrayType( prop );
        }
        return false;
    }

    private boolean isPrimitiveArrayType( Object array )
    {
        if( array instanceof int[] || array instanceof Integer[] ||
            array instanceof String[] || array instanceof boolean[] ||
            array instanceof Boolean[] || array instanceof double[] ||
            array instanceof Double[] || array instanceof float[] ||
            array instanceof Float[] || array instanceof long[] ||
            array instanceof Long[] || array instanceof byte[] ||
            array instanceof Byte[] || array instanceof char[] ||
            array instanceof Character[] || array instanceof short[] ||
            array instanceof Short[] )
        {
            return true;
        }
        return false;
    }

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
            boolean manyAssocNode = false;
            for( Relationship manyRel : endNode.getRelationships( RelTypes.MANY_ASSOCIATION, Direction.OUTGOING ) )
            {
                manyRel.delete();
                manyAssocNode = true;
            }
            if( manyAssocNode )
            {
                endNode.delete();
            }
            rel.delete();
        }
        underlyingNode.delete();
        status = EntityStatus.REMOVED;
    }

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

    public EntityReference identity()
    {
        return new EntityReference( (String) underlyingNode.getProperty( ENTITY_ID ) );
    }

    public boolean isOfType( TypeName type )
    {
        Node typeNode = underlyingNode.getSingleRelationship( RelTypes.IS_OF_TYPE, Direction.OUTGOING ).getEndNode();
        String typeName = (String) typeNode.getProperty( NeoEntityStoreUnitOfWork.ENTITY_TYPE );
        return typeName.equals( type.name() );
    }

    public long lastModified()
    {
        long modified = (Long) underlyingNode.getProperty( MODIFIED );
        return modified;
    }

    public EntityStatus status()
    {
        return status;
    }

    public String version()
    {
        long version = (Long) underlyingNode.getProperty( VERSION );
        if( status == EntityStatus.UPDATED )
        {
            version--;
        }
        return "" + version;
    }

    public EntityStoreUnitOfWork getUnitOfWork()
    {
        return uow;
    }
}