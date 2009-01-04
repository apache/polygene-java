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

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectIdentityList extends AbstractSequentialList<QualifiedIdentity>
{
    private final DirectEntityState state;
    private final NeoService neo;
    private final RelationshipType internalType;
    private final RelationshipType startType;
    private final RelationshipType endType;
    private final String qualifiedName;
    private final NeoIdentityIndex idIndex;

    public DirectIdentityList( NeoService neo, NeoIdentityIndex idIndex, DirectEntityState state, String qualifiedName )
    {
        this.neo = neo;
        this.state = state;
        this.internalType = LinkType.INTERNAL.getRelationshipType( qualifiedName );
        this.startType = LinkType.START.getRelationshipType( qualifiedName );
        this.endType = LinkType.END.getRelationshipType( qualifiedName );
        this.qualifiedName = qualifiedName;
        this.idIndex = idIndex;
    }

    public ListIterator<QualifiedIdentity> listIterator( int i )
    {
        return new NodeChainIterator( i );
    }

    public int size()
    {
        return state.getSizeOfCollection( qualifiedName );
    }

    private class NodeChainIterator implements ListIterator<QualifiedIdentity>
    {
        private int index;
        private Relationship currentRelation;
        private Direction lastDirection = null;

        public NodeChainIterator( int startIndex )
        {
            index = size();
            if( startIndex < 0 || index < startIndex )
            {
                throw new IndexOutOfBoundsException();
            }
            else if( index - startIndex < startIndex )
            {
                // backwards is nearer
                currentRelation = state.underlyingNode.getSingleRelationship( endType, Direction.INCOMING );
                for( ; index > startIndex; index-- )
                {
                    currentRelation = previousRelation( currentRelation.getStartNode() );
                }
            }
            else
            {
                // forwards is nearer
                currentRelation = state.underlyingNode.getSingleRelationship( startType, Direction.OUTGOING );
                for( index = 0; index < startIndex; index++ )
                {
                    currentRelation = nextRelation( currentRelation.getEndNode() );
                }
            }
        }

        public boolean hasNext()
        {
            return currentRelation != null && !currentRelation.isType( endType );
        }

        public QualifiedIdentity next()
        {
            if( hasNext() )
            {
                index++;
                currentRelation = nextRelation( currentRelation.getEndNode() );
                lastDirection = Direction.OUTGOING;
                return DirectEntityState.getIdentityFromNode( unproxy( currentRelation.getStartNode() ) );
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        public boolean hasPrevious()
        {
            return currentRelation != null && !currentRelation.isType( startType );
        }

        public QualifiedIdentity previous()
        {
            if( hasPrevious() )
            {
                index--;
                currentRelation = previousRelation( currentRelation.getStartNode() );
                lastDirection = Direction.INCOMING;
                return DirectEntityState.getIdentityFromNode( unproxy( currentRelation.getEndNode() ) );
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        public int nextIndex()
        {
            return index;
        }

        public int previousIndex()
        {
            return index - 1;
        }

        /*
         * Implementation of setting and removing entities
         */
        private class RelationshipModifier
        {
            private final Relationship last;
            private final Node before;
            private final Node after;
            private final Node removed;

            RelationshipModifier()
            {
                switch( lastDirection )
                {
                case OUTGOING: // remove node returned by next()
                    after = currentRelation.getEndNode();
                    removed = currentRelation.getStartNode();
                    last = previousRelation( removed );
                    before = last.getStartNode();
                    break;
                case INCOMING: // remove node returned by previous()
                    before = currentRelation.getStartNode();
                    removed = currentRelation.getEndNode();
                    last = nextRelation( removed );
                    after = last.getEndNode();
                    break;
                default:
                    throw new IllegalStateException( "Illegal value of lastDirection" );
                }
            }

            Relationship remove()
            {
                RelationshipType type = last.getType();
                last.delete();
                currentRelation.delete();
                if( unproxy( removed ) != removed )
                {
                    removed.delete();
                }
                if( lastDirection == Direction.OUTGOING )
                {
                    index--;
                }
                changeSizeBy( -1 );
                return createRelation( before, type, after );
            }

            Relationship set( Node added )
            {
                RelationshipType beforeType = internalType, afterType = internalType;
                if( before.equals( state.underlyingNode ) )
                {
                    beforeType = startType;
                }
                if( after.equals( state.underlyingNode ) )
                {
                    afterType = endType;
                }
                last.delete();
                currentRelation.delete();
                if( unproxy( removed ) != removed )
                {
                    removed.delete();
                }
                if( inList( added ) )
                {
                    added = proxy( added );
                }
                Relationship to = createRelation( before, beforeType, added );
                Relationship from = createRelation( added, afterType, after );
                if( lastDirection == Direction.OUTGOING )
                { // last was next()
                    return from;
                }
                else
                { // last was previous()
                    return to;
                }
            }
        }

        public void remove()
        {
            if( null == lastDirection )
            {
                throw new IllegalStateException();
            }
            RelationshipModifier modifier = new RelationshipModifier();
            currentRelation = modifier.remove();
            lastDirection = null;
        }

        public void set( QualifiedIdentity qualifiedIdentity )
        {
            if( null == lastDirection )
            {
                throw new IllegalStateException();
            }
            Node added = idIndex.getNode( qualifiedIdentity.identity() );
            RelationshipModifier modifier = new RelationshipModifier();
            currentRelation = modifier.set( added );
            lastDirection = null;
        }

        public void add( QualifiedIdentity qualifiedIdentity )
        {
            Node before, after;
            RelationshipType beforeType = internalType, afterType = internalType;
            if( null == currentRelation )
            {
                before = after = state.underlyingNode;
                beforeType = startType;
                afterType = endType;
            }
            else
            {
                before = currentRelation.getStartNode();
                after = currentRelation.getEndNode();
                currentRelation.delete();
                if( before.equals( state.underlyingNode ) )
                {
                    beforeType = startType;
                }
                else if( after.equals( state.underlyingNode ) )
                {
                    afterType = endType;
                }
            }
            Node added = idIndex.getNode( qualifiedIdentity.identity() );
            if( inList( added ) )
            {
                added = proxy( added );
            }
            createRelation( before, beforeType, added );
            currentRelation = createRelation( added, afterType, after );
            changeSizeBy( 1 );
            index++;
            lastDirection = null;
        }

        // Traversal direction details

        private Relationship nextRelation( Node node )
        {
            return getRelation( node, endType, Direction.OUTGOING );
        }

        private Relationship previousRelation( Node node )
        {
            return getRelation( node, startType, Direction.INCOMING );
        }

        private Relationship createRelation( Node before, RelationshipType type, Node after )
        {
            Relationship relation = before.createRelationshipTo( after, type );
            relation.setProperty( DirectEntityState.ASSOCIATION_OF_PROPERTY_KEY, state.underlyingNode.getId() );
            return relation;
        }

        private boolean inList( Node node )
        {
            if( getRelation( node, startType, Direction.INCOMING ) != null )
            {
                return true;
            }
            else if( getRelation( node, endType, Direction.OUTGOING ) != null )
            {
                return true;
            }
            else if( state.underlyingNode.equals(node) )
            {
            	return true;
            }
            return false;
        }

        private Node proxy( Node original )
        {
            return DirectEntityState.proxy(neo, original);
        }

        private Node unproxy( Node listed )
        {
            return DirectEntityState.unproxy(listed);
        }

        private Relationship getRelation( Node node, RelationshipType edgeType, Direction direction )
        {
            Relationship relation = getRelationFrom( node.getRelationships( edgeType, direction ) );
            if( null == relation )
            {
                relation = getRelationFrom( node.getRelationships( internalType, direction ) );
            }
            return relation;
        }

        private Relationship getRelationFrom( Iterable<Relationship> relationships )
        {
            Iterator<Relationship> iterator = relationships.iterator();
            if( !iterator.hasNext() )
            {
                return null;
            }
            Relationship relation = iterator.next();
            if( !iterator.hasNext() )
            {
                return relation;
            }
            while( iterator.hasNext() )
            {
                if( applicable( relation ) )
                {
                    return relation;
                }
                relation = iterator.next();
            }
            return null;
        }

        private boolean applicable( Relationship relation )
        {
            final long relatedTo = (Long) relation.getProperty( DirectEntityState.ASSOCIATION_OF_PROPERTY_KEY );
            return relatedTo == state.underlyingNode.getId();
        }
    }

    private void changeSizeBy( int delta )
    {
        state.setSizeOfCollection( qualifiedName, size() + delta );
    }
}
