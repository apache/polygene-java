package org.qi4j.functional;

/**
 * Generic Hierarchical Visitor interface.
 */
public class HierarchicalVisitorAdapter<NODE, LEAF, ThrowableType extends Throwable>
        implements HierarchicalVisitor<NODE,LEAF,ThrowableType>
{
    @Override
    public boolean visitEnter( NODE visited )
            throws ThrowableType
    {
        return true;
    }

    @Override
    public boolean visitLeave( NODE visited )
            throws ThrowableType
    {
        return true;
    }

    @Override
    public boolean visit( LEAF visited ) throws ThrowableType
    {
        return true;
    }
}
