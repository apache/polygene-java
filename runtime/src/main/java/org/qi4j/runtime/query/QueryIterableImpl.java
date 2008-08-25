/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.qi4j.property.Property;
import org.qi4j.query.Query;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.query.grammar.PropertyReference;

/**
 * TODO
 */
public class QueryIterableImpl<T>
    implements Query<T>
{
    private static final long serialVersionUID = 1L;

    /**
     * Type of queried entities.
     */
    private final Class<T> resultType;

    /**
     * Order by clause segments.
     */
    private OrderBy[] orderBySegments;

    /**
     * First result to be returned.
     */
    private Integer firstResult;

    /**
     * Maximum number of results to be returned.
     */
    private Integer maxResults;

    private Iterable<T> iterable;

    /**
     * Constructor.
     *
     * @param resultType type of queried entities; cannot be null
     */
    QueryIterableImpl( final Iterable<T> iterable,
                       final Class<T> resultType )
    {
        this.iterable = iterable;
        this.resultType = resultType;
    }

    /**
     * @see Query#orderBy(OrderBy[])
     */
    public Query<T> orderBy( final OrderBy... segments )
    {
        orderBySegments = segments;
        return this;
    }

    /**
     * @see Query#firstResult(int)
     */
    public Query<T> firstResult( int firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @see Query#maxResults(int)
     */
    public Query<T> maxResults( int maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @see Query#find()
     */
    public T find()
    {
        Iterator<T> iterator = iterator();

        if (iterator.hasNext())
            return iterator.next();
        else
            return null;
    }

    /**
     * @see Query#setVariable(String, Object)
     */
    @SuppressWarnings( "unchecked" )
    public void setVariable( final String name, final Object value )
    {
        // Ignore
    }

    /**
     * @see Query#getVariable(String)
     */
    @SuppressWarnings( "unchecked" )
    public <V> V getVariable( final String name )
    {
        return null;
    }

    public Class<T> resultType()
    {
        return resultType;
    }

    /**
     * @see Query#iterator()
     */
    public Iterator<T> iterator()
    {
        // Ensure it's a list first
        List<T> list = toList();

        // Order list
        if( orderBySegments != null )
        {
            // Copy list
            list = new ArrayList<T>( list );

            // Sort it
            Collections.sort( list, new OrderByComparator<T>());
        }

        // Cut results
        if( firstResult != null )
        {
            if( firstResult > list.size() )
            {
                list = Collections.emptyList();
                return list.iterator();
            }

            int toIdx;
            if( maxResults != null )
            {
                toIdx = Math.min( firstResult + maxResults, list.size() );
            }
            else
            {
                toIdx = list.size();
            }

            list = list.subList( firstResult, toIdx );
        } else
        {
            int toIdx;
            if( maxResults != null )
            {
                toIdx = Math.min( maxResults, list.size() );
            }
            else
            {
                toIdx = list.size();
            }

            list = list.subList( 0, toIdx );
        }

        return list.iterator();
    }

    @Override public String toString()
    {
        return "Iterable query of type " + resultType.getName();
    }

    public long count()
    {
        long count = 0;
        if (iterable instanceof Collection )
        {
            Collection collection = (Collection) iterable;

            count = collection.size();

            if (firstResult != null)
            {
                if (firstResult < count)
                    count = count - firstResult;
                else
                    count = 0;
            }

            if (maxResults != null)
                count = Math.min( count, maxResults );
        } else
        {
            for( T t : iterable )
            {
                count++;
            }

            if (firstResult != null)
            {
                if (firstResult < count)
                    count = count - firstResult;
                else
                    count = 0;
            }

            if (maxResults != null)
                count = Math.min( count, maxResults );
        }

        return count;
    }

    private List<T> toList()
    {
        List<T> list;
        if (iterable instanceof List)
        {
            list = (List<T>) iterable;

        } else
        {
            list = new ArrayList<T>();
            for( T t : iterable )
            {
                list.add(t);
            }
        }
        return list;
    }

    public class OrderByComparator<T>
        implements Comparator<T>
    {

        public int compare( T o1, T o2 )
        {
            for( int i = 0; i < orderBySegments.length; i++ )
            {
                OrderBy orderBySegment = orderBySegments[ i ];
                PropertyReference propertyRef = orderBySegment.propertyReference();
                Method accessor = propertyRef.propertyAccessor();

                try
                {
                    Comparable<T> value1 = (Comparable<T>) ( (Property) accessor.invoke( o1 ) ).get();
                    T value2 = (T) ( (Property) accessor.invoke( o2 ) ).get();

                    int val = value1.compareTo( value2 );
                    if( val != 0 )
                    {
                        if (orderBySegment.order() == OrderBy.Order.ASCENDING)
                            return val;
                        else
                            return -val;
                    }
                }
                catch( Exception e )
                {
                    return 0;
                }
            }

            return 0;
        }
    }
}