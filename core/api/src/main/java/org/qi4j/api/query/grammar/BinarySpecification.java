package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;

/**
 * Base binary Specification, used for AND and OR Specifications..
 */
public abstract class BinarySpecification
    implements Predicate<Composite>
{
    protected final Iterable<Predicate<Composite>> operands;

    protected BinarySpecification( Iterable<Predicate<Composite>> operands )
    {
        this.operands = operands;
    }

    public Iterable<Predicate<Composite>> operands()
    {
        return operands;
    }
}
