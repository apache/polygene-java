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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser;

public class UncreatedNode implements Node
{
    private static final Map<String, UncreatedNode> uncreated = new ConcurrentHashMap<String, UncreatedNode>();
    private static final Traverser NO_TRAVERSER = new Traverser()
    {
        private final Iterable<Node> iter = nullIterable( Node.class );

        public TraversalPosition currentPosition()
        {
            throw new IllegalStateException();
        }

        public Collection<Node> getAllNodes()
        {
            return Collections.emptyList();
        }

        public Iterator<Node> iterator()
        {
            return iter.iterator();
        }
    };

    public static Node getNode( String key, NeoService neo )
    {
        UncreatedNode result = uncreated.get( key );
        if( result == null )
        {
            synchronized( uncreated )
            {
                result = uncreated.get( key );
                if( result == null )
                {
                    result = new UncreatedNode( neo, key );
                    uncreated.put( key, result );
                }
            }
        }
        return result;
    }

    private final NeoService neo;
    private final String key;
    private volatile Node actual = null;
    private boolean deleted = false;

    private UncreatedNode( NeoService neo, String key )
    {
        this.neo = neo;
        this.key = key;
    }

    private Node node()
    {
        Node node = actual;
        if( node == null && !deleted )
        {
            synchronized( this )
            {
                node = actual;
                if( node == null && !deleted )
                {
                    uncreated.remove( key );
                    actual = node = neo.createNode();
                }
            }
        }
        return node;
    }


    @Override
    public boolean equals( Object obj )
    {

        return obj instanceof Node && ( (Node) obj ).getId() == getId();
    }

    public long getId()
    {
        return node().getId();
    }

    public void delete()
    {
        Node node = actual;
        if( node != null )
        {
            node.delete();
        }
        deleted = true;
    }

    public Iterable<Relationship> getRelationships()
    {
        Node node = actual;
        if( node != null )
        {
            return node.getRelationships();
        }
        else
        {
            return nullIterable( Relationship.class );
        }
    }

    private static <E> Iterable<E> nullIterable( Class<E> type )
    {
        return new Iterable<E>()
        {
            public Iterator<E> iterator()
            {
                return new Iterator<E>()
                {
                    public boolean hasNext()
                    {
                        return false;
                    }

                    public E next()
                    {
                        throw new NoSuchElementException();
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public boolean hasRelationship()
    {
        Node node = actual;
        return node != null && node.hasRelationship();
    }

    public Iterable<Relationship> getRelationships( RelationshipType... relationshipTypes )
    {
        Node node = actual;
        if( node != null )
        {
            return node.getRelationships( relationshipTypes );
        }
        else
        {
            return nullIterable( Relationship.class );
        }
    }

    public boolean hasRelationship( RelationshipType... relationshipTypes )
    {
        Node node = actual;
        return node != null && node.hasRelationship( relationshipTypes );
    }

    public Iterable<Relationship> getRelationships( Direction direction )
    {
        Node node = actual;
        if( node != null )
        {
            return node.getRelationships( direction );
        }
        else
        {
            return nullIterable( Relationship.class );
        }
    }

    public boolean hasRelationship( Direction direction )
    {
        Node node = actual;
        return node != null && node.hasRelationship( direction );
    }

    public Iterable<Relationship> getRelationships( RelationshipType relationshipType, Direction direction )
    {
        Node node = actual;
        if( node != null )
        {
            return node.getRelationships( relationshipType, direction );
        }
        else
        {
            return nullIterable( Relationship.class );
        }
    }

    public boolean hasRelationship( RelationshipType relationshipType, Direction direction )
    {
        Node node = actual;
        return node != null && node.hasRelationship( relationshipType, direction );
    }

    public Relationship getSingleRelationship( RelationshipType relationshipType, Direction direction )
    {
        Node node = actual;
        if( node != null )
        {
            return node.getSingleRelationship( relationshipType, direction );
        }
        else
        {
            return null;
        }
    }

    public Relationship createRelationshipTo( Node node, RelationshipType relationshipType )
    {
        return node().createRelationshipTo( node, relationshipType );
    }

    public boolean hasProperty( String s )
    {
        Node node = actual;
        return node != null && node.hasProperty( s );
    }

    public Object getProperty( String s )
    {
        Node node = actual;
        if( node != null )
        {
            return node.getProperty( s );
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    public Object getProperty( String s, Object o )
    {
        Node node = actual;
        if( node != null )
        {
            return node.getProperty( s, o );
        }
        else
        {
            return o;
        }
    }

    public void setProperty( String s, Object o )
    {
        node().setProperty( s, o );
    }

    public Object removeProperty( String s )
    {
        return node().removeProperty( s );
    }

    public Iterable<String> getPropertyKeys()
    {
        Node node = actual;
        if( node != null )
        {
            return node.getPropertyKeys();
        }
        else
        {
            return nullIterable( String.class );
        }
    }

    public Iterable<Object> getPropertyValues()
    {
        Node node = actual;
        if( node != null )
        {
            return node.getPropertyValues();
        }
        else
        {
            return nullIterable( Object.class );
        }
    }

    public Traverser traverse( Traverser.Order order, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType relationshipType, Direction direction )
    {
        Node node = actual;
        if( node != null )
        {
            return node.traverse( order, stopEvaluator, returnableEvaluator, relationshipType, direction );
        }
        else
        {
            return NO_TRAVERSER;
        }
    }

    public Traverser traverse( Traverser.Order order, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType relationshipType, Direction direction, RelationshipType relationshipType1, Direction direction1 )
    {
        Node node = actual;
        if( node != null )
        {
            return node.traverse( order, stopEvaluator, returnableEvaluator, relationshipType, direction, relationshipType1, direction1 );
        }
        else
        {
            return NO_TRAVERSER;
        }
    }

    public Traverser traverse( Traverser.Order order, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, Object... objects )
    {
        Node node = actual;
        if( node != null )
        {
            return node.traverse( order, stopEvaluator, returnableEvaluator, objects );
        }
        else
        {
            return NO_TRAVERSER;
        }
    }
}
