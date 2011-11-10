package org.qi4j.functional;

/**
 * Generic Hierarchical Visitor interface.
 */
public class HierarchicalVisitor<NODE, LEAF, ThrowableType extends Throwable>
        implements Visitor<LEAF, ThrowableType>
{
    /**
     * Enter an instance of T
     *
     * @param visited the visited instance which is now entered
     * @return true if the visitor pattern should continue, false if it should be aborted for this level
     * @throws ThrowableType if an exception occurred during processing. Any client call that initiated the visiting should
     *                       get the exception in order to handle it properly.
     */
    public boolean visitEnter( NODE visited )
            throws ThrowableType
    {
        return true;
    }

    /**
     * Leave an instance of T
     *
     * @param visited the visited instance which is now left
     * @return true if the visitor pattern should continue, false if it should be aborted for the level of this node
     * @throws ThrowableType if an exception occurred during processing. Any client call that initiated the visiting should
     *                       get the exception in order to handle it properly.
     */
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
