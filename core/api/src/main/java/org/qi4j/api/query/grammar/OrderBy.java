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
package org.qi4j.api.query.grammar;

/**
 * Query sorting segment.
 */
public class OrderBy
{
    /**
     * Order direction.
     */
    public enum Order
    {
        ASCENDING, DESCENDING
    }

    /**
     * Order.
     */
    private final PropertyFunction<?> propertyReference;
    /**
     * Direction.
     */
    private final Order order;

    /**
     * Constructor.
     *
     * @param propertyReference property that determines the order; cannot be null
     * @param order             direction
     *
     * @throws IllegalArgumentException - If property is null
     */
    public OrderBy( final PropertyFunction<?> propertyReference,
                    final Order order
    )
    {
        if( propertyReference == null )
        {
            throw new IllegalArgumentException( "Ordering property cannot be null" );
        }
        this.propertyReference = propertyReference;
        this.order = order;
    }

    /**
     * Getter.
     *
     * @return property; cannot be null
     */
    public PropertyFunction<?> property()
    {
        return propertyReference;
    }

    /**
     * Getter.
     *
     * @return direction; cannot be null
     */
    public Order order()
    {
        return order;
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( propertyReference )
            .append( " " )
            .append( order )
            .toString();
    }
}