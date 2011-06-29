package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;

import static org.qi4j.api.util.Iterables.iterable;

/**
 * TODO
 */
public abstract class ExpressionSpecification
    implements Specification<Composite>
{
    public AndSpecification and(Specification<Composite> specification)
    {
        if (this instanceof AndSpecification)
        {
            return new AndSpecification( Iterables.append( specification, ((AndSpecification)this).getOperands() ));

        } else
            return new AndSpecification( iterable( this, specification ) );
    }

    public OrSpecification or(Specification<Composite> specification)
    {
        if (this instanceof OrSpecification)
        {
            return new OrSpecification( Iterables.append( specification, ((OrSpecification)this).getOperands() ));

        } else
            return new OrSpecification( iterable( this, specification ) );
    }
}
