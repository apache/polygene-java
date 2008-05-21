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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.CompositeDescriptor;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public abstract class NeoEntityState implements EntityState
{
    public static final RelationshipType PROXY_FOR = new AssociationRelationshipType( "<PROXY FOR>" );
    private static final String VERSION_PROPERTY_KEY = "entityVersion";
    public static final String ASSOCIATION_OF_PROPERTY_KEY = "associationOf";
    private static final String COLLECTION_SIZE_PROPERTY_POSTFIX = "sizeOf::";
    private static final String IDENTITY_PROPERTY = "identity";
    public static final String TYPE_PROPERTY = "compositeType";
    private static final RelationshipType COMPOSITE_TYPE_RELATIONSHIP_TYPE = new AssociationRelationshipType( "<TYPE>" );
    private static final Set<Class> legalPropertyValues = new HashSet<Class>()
    {
        {
            add( String.class );
            add( String[].class );
            add( int.class );
            add( int[].class );
            add( Integer.class );
            add( Integer[].class );
            add( long.class );
            add( long[].class );
            add( Long.class );
            add( Long[].class );
            add( boolean.class );
            add( boolean[].class );
            add( Boolean.class );
            add( Boolean[].class );
            add( short.class );
            add( short[].class );
            add( Short.class );
            add( Short[].class );
            add( char.class );
            add( char[].class );
            add( Character.class );
            add( Character[].class );
            add( byte.class );
            add( byte[].class );
            add( Byte.class );
            add( Byte[].class );
            add( double.class );
            add( double[].class );
            add( Double.class );
            add( Double[].class );
            add( float.class );
            add( float[].class );
            add( Float.class );
            add( Float[].class );
        }
    };
    // Library methods

    protected static long getVersionFromNode( Node node )
    {
        return (Long) node.getProperty( VERSION_PROPERTY_KEY, 0L );
    }

    protected static void setVersionToNode( Node node, long value )
    {
        node.setProperty( VERSION_PROPERTY_KEY, value );
    }

    // State
    protected final List<String> properties;
    protected final List<String> associationModels;
    protected final Map<String, AssociationModel> manyAssociationsModels;

    public final Node underlyingNode;
    public final NeoIdentityIndex idIndex;
    protected final QualifiedIdentity identity;
    private EntityStatus status;

    // Construction and Initialization

    protected NeoEntityState( NeoEntityState template, CompositeDescriptor descriptor )
    {
        this( template.idIndex, template.underlyingNode, template.identity, template.status, descriptor );
    }

    protected NeoEntityState( NeoIdentityIndex idIndex, Node underlyingNode, QualifiedIdentity identity, EntityStatus status, CompositeDescriptor descriptor )
    {
        this.idIndex = idIndex;
        this.underlyingNode = underlyingNode;
        this.identity = identity;
        this.status = status;
        List<String> properties = new LinkedList<String>();
        List<String> associationModels = new LinkedList<String>();
        Map<String, AssociationModel> manyAssociationsModels = new HashMap<String, AssociationModel>();
        // TODO: associations and properties could be stored as a signature in the Node,
        // or rather in a type Node [for this we need a type index], so that when entities evolve,
        // the presistence model will be able to evolv more easily as well.
        for( AssociationModel assoc : descriptor.getCompositeModel().getAssociationModels() )
        {
            if( isManyAssociation( assoc ) )
            {
                manyAssociationsModels.put( assoc.getQualifiedName(), assoc );
            }
            else
            {
                associationModels.add( assoc.getQualifiedName() );
            }
        }
        for( PropertyModel prop : descriptor.getCompositeModel().getPropertyModels() )
        {
            properties.add( prop.getQualifiedName() );
        }
        this.properties = Collections.unmodifiableList( properties );
        this.associationModels = Collections.unmodifiableList( associationModels );
        this.manyAssociationsModels = Collections.unmodifiableMap( manyAssociationsModels );
    }

    // EntityState implementation

    public final QualifiedIdentity getIdentity()
    {
        return identity;
    }

    public final EntityStatus getStatus()
    {
        return status;
    }

    public final void setProperty( String qualifiedName, Object newValue )
    {
        verifyPropertyValue( newValue );
        storeProperty( qualifiedName, newValue );
    }

    public final Iterable<String> getPropertyNames()
    {
        return properties;
    }

    public final Iterable<String> getAssociationNames()
    {
        return associationModels;
    }

    public final Iterable<String> getManyAssociationNames()
    {
        return manyAssociationsModels.keySet();
    }

    protected final long getNodeVersion()
    {
        return getVersionFromNode( underlyingNode );
    }

    public final long incNodeVersion()
    {
        long oldVersion = getVersionFromNode( underlyingNode );
        setVersionToNode( underlyingNode, oldVersion + 1 );
        return oldVersion;
    }

    public final void createAssociation( AssociationModel model, QualifiedIdentity id )
    {
        createAssociation( model.getQualifiedName(), id, true );
    }

    protected final void createAssociation( String qualifiedName, QualifiedIdentity newEntity, boolean multipleAssociation )
    {
        RelationshipType associationType = getAssociationType( qualifiedName );
        if( multipleAssociation )
        {
            Relationship relation = underlyingNode.getSingleRelationship( associationType, Direction.OUTGOING );
            if( relation != null )
            {
                relation.delete();
            }
        }
        Node newNode = idIndex.getNode( newEntity.getIdentity() );
        underlyingNode.createRelationshipTo( newNode, associationType );
    }

    protected final Collection<QualifiedIdentity> createManyAssociation( String qualifiedName )
    {
        AssociationModel model = manyAssociationsModels.get( qualifiedName );
        if( model == null )
        {
            throw new IllegalArgumentException( "No such association: " + qualifiedName );
        }
        return IdentifierCollectionFactory.createCollection( this, model );
    }

    public final Collection<QualifiedIdentity> setManyAssociation( String qualifiedName, Collection<QualifiedIdentity> newManyAssociation )
    {
        throw new UnsupportedOperationException();
    }

    // Child contract

    protected abstract void storeProperty( String qualifiedName, Object value );

    public abstract void prepareCommit();

    // Implementation internals

    public void markForRemoval()
    {
        status = EntityStatus.REMOVED;
    }

    public static boolean isManyAssociation( AssociationModel model )
    {
        return ManyAssociation.class.isAssignableFrom( model.getAccessor().getReturnType() );
    }

    private void verifyPropertyValue( Object value )
    {
        if( !legalPropertyValues.contains( value.getClass() ) )
        {
            throw new IllegalArgumentException( "Cannot store " + value.getClass() + " objects as properties." );
        }
    }

    public Relationship createInternalLink( QualifiedIdentity start, AssociationModel model, QualifiedIdentity end )
    {
        return createLink( start, getAssociationType( model, LinkType.INTERNAL ), end );
    }

    public Relationship createStartLink( QualifiedIdentity start, AssociationModel model, QualifiedIdentity end )
    {
        return createLink( start, getAssociationType( model, LinkType.START ), end );
    }

    public Relationship createEndLink( QualifiedIdentity start, AssociationModel model, QualifiedIdentity end )
    {
        return createLink( start, getAssociationType( model, LinkType.END ), end );
    }

    private Relationship createLink( QualifiedIdentity start, RelationshipType associationType, QualifiedIdentity end )
    {
        Node startNode = idIndex.getNode( start.getIdentity() );
        Node endNode = idIndex.getNode( end.getIdentity() );
        Relationship association = startNode.createRelationshipTo( endNode, associationType );
        association.setProperty( ASSOCIATION_OF_PROPERTY_KEY, underlyingNode.getId() );
        return association;
    }

    public Iterable<Relationship> getRelationships( AssociationModel model )
    {
        RelationshipType associationType = getAssociationType( model, LinkType.UNQUALIFIED );
        return underlyingNode.getRelationships( associationType, Direction.OUTGOING );
    }

    protected final QualifiedIdentity getSingleAssociation( String qualifiedName )
    {
        RelationshipType associationType = getAssociationType( qualifiedName );
        Relationship relation = underlyingNode.getSingleRelationship( associationType, Direction.OUTGOING );
        return getIdentityFromNode( relation.getEndNode() );
    }

    public int getSizeOfCollection( AssociationModel model )
    {
        return (Integer) underlyingNode.getProperty( COLLECTION_SIZE_PROPERTY_POSTFIX + model.getQualifiedName(), 0 );
    }

    public void setSizeOfCollection( AssociationModel model, int size )
    {
        underlyingNode.setProperty( COLLECTION_SIZE_PROPERTY_POSTFIX + model.getQualifiedName(), size );
    }

    public Relationship createLink( QualifiedIdentity qualifiedIdentity, AssociationModel model )
    {
        RelationshipType associationType = getAssociationType( model, LinkType.UNQUALIFIED );
        Node node = idIndex.getNode( qualifiedIdentity.getIdentity() );
        return underlyingNode.createRelationshipTo( node, associationType );
    }

    protected void storeIdentity()
    {
        underlyingNode.setProperty( IDENTITY_PROPERTY, identity.getIdentity() );
        Node typeNode = idIndex.getTypeNode( identity.getCompositeType() );
        underlyingNode.createRelationshipTo( typeNode, COMPOSITE_TYPE_RELATIONSHIP_TYPE );
    }

    public static QualifiedIdentity getIdentityFromNode( Node node )
    {
        String id = (String) node.getProperty( IDENTITY_PROPERTY );
        Node typeNode = node.getSingleRelationship( COMPOSITE_TYPE_RELATIONSHIP_TYPE, Direction.OUTGOING ).getEndNode();
        String clazz = (String) typeNode.getProperty( TYPE_PROPERTY );
        return new QualifiedIdentity( id, clazz );
    }

    public static RelationshipType getAssociationType( AssociationModel model, LinkType linkType )
    {
        return getAssociationType( linkType.getRelationshipTypeName( model ) );
    }

    protected static RelationshipType getAssociationType( String typeName )
    {
        return new AssociationRelationshipType( typeName );
    }

    private static class AssociationRelationshipType implements RelationshipType
    {
        private final String name;

        public AssociationRelationshipType( String name )
        {
            this.name = name;
        }

        public String name()
        {
            return name;
        }
    }
}
