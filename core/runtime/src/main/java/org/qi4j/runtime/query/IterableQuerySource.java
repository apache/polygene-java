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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;
import org.qi4j.spi.query.QuerySource;

/**
 * JAVADOC
 */
public class IterableQuerySource
    implements QuerySource
{
    private Iterable iterable;

    /**
     * Constructor.
     *
     * @param iterable iterable
     */
    IterableQuerySource( final Iterable iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public <T> T find( Class<T> resultType,
                       Specification<Composite> whereClause,
                       Iterable<OrderBy> orderBySegments,
                       Integer firstResult,
                       Integer maxResults,
                       Map<String, Object> variables
    )
    {
        final Iterator<T> iterator = iterator( resultType, whereClause, orderBySegments, firstResult, maxResults, variables );
        if( iterator.hasNext() )
        {
            return iterator.next();
        }
        return null;
    }

    @Override
    public <T> long count( Class<T> resultType,
                           Specification<Composite> whereClause,
                           Iterable<OrderBy> orderBySegments,
                           Integer firstResult,
                           Integer maxResults,
                           Map<String, Object> variables
    )
    {
        return list( resultType, whereClause, orderBySegments, firstResult, maxResults, variables ).size();
    }

    @Override
    public <T> Iterator<T> iterator( Class<T> resultType,
                                     Specification<Composite> whereClause,
                                     Iterable<OrderBy> orderBySegments,
                                     Integer firstResult,
                                     Integer maxResults,
                                     Map<String, Object> variables
    )
    {
        return list( resultType, whereClause, orderBySegments, firstResult, maxResults, variables ).iterator();
    }

    private <T> List<T> list( Class<T> resultType,
                              Specification<Composite> whereClause,
                              Iterable<OrderBy> orderBySegments,
                              Integer firstResult,
                              Integer maxResults,
                              Map<String, Object> variables
    )
    {
        // Ensure it's a list first
        List<T> list = filter( resultType, whereClause );

        // Order list
        if( orderBySegments != null )
        {
            // Sort it
            Collections.sort( list, new OrderByComparator( orderBySegments ) );
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

    private <T> List<T> filter( Class<T> resultType, Specification whereClause )
    {
        if( whereClause == null )
        {
            return Iterables.toList( Iterables.filter( Classes.instanceOf( resultType ), iterable ) );
        }
        else
        {
            return Iterables.toList( Iterables.filter( Specifications.and( Classes.instanceOf( resultType ), whereClause ), iterable ) );
        }
    }

    @Override
    public String toString()
    {
        return "IterableQuerySource{" + iterable + '}';
    }

    private static class OrderByComparator<T extends Composite>
        implements Comparator<T>
    {

        private Iterable<OrderBy> orderBySegments;

        private OrderByComparator( Iterable<OrderBy> orderBySegments )
        {
            this.orderBySegments = orderBySegments;
        }

        @Override
        public int compare( T o1, T o2 )
        {
            for( OrderBy orderBySegment : orderBySegments )
            {
                try
                {
                    final Property prop1 = orderBySegment.property().map( o1 );
                    final Property prop2 = orderBySegment.property().map( o2 );
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
            }

            return 0;
        }
    }
}