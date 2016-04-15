/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.demo.tenminute;

import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.elsewhere.inventory.InventoryService;

// START SNIPPET: allClass
public class InventoryConcern extends ConcernOf<Order>
    implements Order
{
    @Service
    private InventoryService inventory;

    @Override
    public void addLineItem( LineItem item )
    {
        String productCode = item.productCode().get();
        int quantity = item.quantity().get();
        inventory.remove( productCode, quantity );
        next.addLineItem( item );
    }

    @Override
    public void removeLineItem( LineItem item )
    {
        String productCode = item.productCode().get();
        int quantity = item.quantity().get();
        inventory.add( productCode, quantity );
        next.removeLineItem( item );
    }
}
// END SNIPPET: allClass
