package org.qi4j.manual.recipes.concern;

public class InventoryConcern
    implements Order
{
    @Override
    public void addLineItem( LineItem item )
    {
    }

    @Override
    public void removeLineItem( LineItem item )
    {
    }
}
