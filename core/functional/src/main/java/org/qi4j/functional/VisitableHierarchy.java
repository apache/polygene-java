package org.qi4j.functional;

/**
 * Interface that visitable hierarchies of objects should implement.
 */
public interface VisitableHierarchy<NODE, LEAF>
{
    <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super NODE, ? super LEAF, ThrowableType> visitor )
        throws ThrowableType;
}
