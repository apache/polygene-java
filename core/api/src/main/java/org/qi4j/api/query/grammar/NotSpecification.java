package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

/**
 * NOT Specification.
 */
public class NotSpecification implements Specification<Composite>
{
    private Specification<Composite> operand;

    public NotSpecification( Specification<Composite> operand )
    {
        this.operand = operand;
    }

    public Specification<Composite> operand()
    {
        return operand;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        return Specifications.not( operand ).satisfiedBy( item );
    }

    @Override
    public String toString()
    {
        return "!" + operand.toString();
    }
}
