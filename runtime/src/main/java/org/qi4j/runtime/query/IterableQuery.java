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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;

/**
 * JAVADOC
 */
public class IterableQuery<T>
    extends AbstractQuery<T>
{
    private static final long serialVersionUID = 1L;

    private Iterable<T> iterable;

    /**
     * Constructor.
     *
     * @param iterable    iterable
     * @param resultType  type of queried entities; cannot be null
     * @param whereClause where clause
     */
    IterableQuery( final Iterable<T> iterable,
                   final Class<T> resultType,
                   final BooleanExpression whereClause
    )
    {
        super( resultType, whereClause );
        this.iterable = iterable;
    }

    /**
     * @see Query#find()
     */
    public T find()
    {
        final Iterator<T> iterator = iterator();
        if( iterator.hasNext() )
        {
            return iterator.next();
        }
        return null;
    }

    /**
     * @see Query#iterator()
     */
    public Iterator<T> iterator()
    {
        return list().iterator();
    }

    private List<T> list()
    {
        // Ensure it's a list first
        List<T> list = filter( toList() );

        // Order list
        if( orderBySegments != null )
        {
            // Sort it
            Collections.sort( list, new OrderByComparator() );
        }

        // Cut results
        if( firstResult != null )
        {
            if( firstResult > list.size() )
            {
                return Collections.emptyList();
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
        }
        else
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

        return list;
    }

    private List<T> filter( final List<T> list )
    {
        final List<T> filtered = new ArrayList<T>();
        if( list != null && list.size() > 0 )
        {
            for( T entry : list )
            {
                if( whereClause == null || whereClause.eval( entry ) )
                {
                    filtered.add( entry );
                }
            }
        }
        return filtered;
    }

    /**
     * JAVADOC not very effcient as caling count more times will rerun the query
     *
     * @see Query#count()
     */
    public long count()
    {
        return list().size();
    }

    private List<T> toList()
    {
        List<T> list;
        if( iterable instanceof List )
        {
            list = (List<T>) iterable;
        }
        else
        {
            list = new ArrayList<T>();
            for( T t : iterable )
            {
                list.add( t );
            }
        }
        return list;
    }

    @Override
    public String toString()
    {
        return "Iterable query of type " + resultType.getName();
    }

    private class OrderByComparator
        implements Comparator<T>
    {

        public int compare( T o1, T o2 )
        {
            int i = 0;
            while( i < orderBySegments.length )
            {
                OrderBy orderBySegment = orderBySegments[ i ];
                try
                {
                    final Property prop1 = orderBySegment.propertyReference().eval( o1 );
                    final Property prop2 = orderBySegment.propertyReference().eval( o2 );
                    if( prop1 == null || prop2 == null )
                    {
                        if( prop1 == null && prop2 == null )
                        {
                            return 0;
                        }
                        else if( prop1 != null )
                        {
                            return 1;
                        }
                        return -1;
                    }
                    final Object value1 = prop1.get();
                    final Object value2 = prop2.get();
                    if( value1 == null || value2 == null )
                    {
                        if( value1 == null && value2 == null )
                        {
                            return 0;
                        }
                        else if( value1 != null )
                        {
                            return 1;
                        }
                        return -1;
                    }
                    if( value1 instanceof Comparable )
                    {
                        int result = ( (Comparable) value1 ).compareTo( value2 );
                        if( result != 0 )
                        {
                            if( orderBySegment.order() == OrderBy.Order.ASCENDING )
                            {
                                return result;
                            }
                            else
                            {
                                return -result;
                            }
                        }
                    }
                }
                catch( Exception e )
                {
                    return 0;
                }
                i++;
            }

            return 0;
        }
    }
}