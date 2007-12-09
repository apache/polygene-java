/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.query;

import java.util.Comparator;
import java.util.List;
import org.qi4j.query.value.MethodCallExpression;

/**
 * TODO
 */
class OrderByComparator
    implements Comparator
{
    List<OrderBy> orderBy;

    public OrderByComparator( List<OrderBy> orderBy )
    {
        this.orderBy = orderBy;
    }

    public int compare( Object o1, Object o2 )
    {
        for( OrderBy by : orderBy )
        {
            MethodCallExpression mce = by.getExpression();
            Comparable v1 = (Comparable) mce.getValue( o1, null);
            Comparable v2 = (Comparable) mce.getValue( o2, null);
            int comparison = v1.compareTo( v2 );
            if ( comparison != 0)
                return by.getOrder() == OrderBy.Order.ASCENDING ? comparison : -comparison; // Consider sort direction

            // If objects are equal, continue with next OrderBy rule
        }

        // Objects are equal!
        return 0;
    }
}
