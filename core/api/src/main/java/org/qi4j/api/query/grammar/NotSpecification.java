package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specifications;

/**
 * NOT Specification.
 */
public class NotSpecification implements Predicate<Composite>
{
    private Predicate<Composite> operand;

    public NotSpecification( Predicate<Composite> operand )
    {
        this.operand = operand;
    }

    public Predicate<Composite> operand()
    {
        return operand;
    }

    @Override
    public boolean test( Composite item )
    {
        return Specifications.not( operand ).test( item );
    }

    @Override
    public String toString()
    {
        return "!" + operand.toString();
    }
}
