package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specification;

/**
 * Base binary Specification, used for AND and OR Specifications..
 */
public abstract class BinarySpecification
    extends ExpressionSpecification
{
    protected final Iterable<Specification<Composite>> operands;

    protected BinarySpecification( Iterable<Specification<Composite>> operands )
    {
        this.operands = operands;
    }

    public Iterable<Specification<Composite>> operands()
    {
        return operands;
    }
}
