/*
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.query.graph;

/**
 * Query sorting segment.
 *
 * @author Niclas Hedhman
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public class OrderBy
{
    /**
     * Order.
     */
    private final PropertyExpression property;
    /**
     * Direction.
     */
    private final Order order;

    /**
     * Constructor. Ascending order.
     *
     * @param property property that determines the order; cannot be null
     * @throws IllegalArgumentException - If property is null
     */
    public OrderBy( final PropertyExpression property )
    {
        this( property, null );
    }

    /**
     * Constructor.
     *
     * @param property property that determines the order; cannot be null
     * @param order    direction
     * @throws IllegalArgumentException - If property is null
     */
    public OrderBy( final PropertyExpression property,
                    final Order order )
    {
        if( property == null )
        {
            throw new IllegalArgumentException( "Ordering property cannot be null" );
        }
        this.property = property;
        this.order = order == null ? Order.ASCENDING : order;
    }

    /**
     * Getter.
     *
     * @return property; cannot be null
     */
    public PropertyExpression getProperty()
    {
        return property;
    }

    /**
     * Getter.
     *
     * @return direction; cannot be null
     */
    public Order getOrder()
    {
        return order;
    }

    @Override public String toString()
    {
        return new StringBuilder()
            .append( property )
            .append( " " )
            .append( order )
            .toString();
    }

    /**
     * Order direction.
     */
    public enum Order
    {
        ASCENDING, DESCENDING
    }

}