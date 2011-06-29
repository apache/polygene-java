package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;

/**
* TODO
*/
public class NotSpecification implements Specification<Composite>
{
    private Specification<Composite> operand;

    public NotSpecification( Specification<Composite> operand )
    {
        this.operand = operand;
    }

    public Specification<Composite> getOperand()
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
        return "!"+operand.toString();
    }
}
