package org.qi4j.manual.recipes.concern;

import org.qi4j.api.concern.Concerns;

// START SNIPPET: class
@Concerns( InventoryConcern.class )
public interface Order
{
    void addLineItem( LineItem item );
    void removeLineItem( LineItem item );

// START SNIPPET: class
// END SNIPPET: class
}
// END SNIPPET: class
