package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

/**
* TODO
*/
public class AndSpecification
    extends BinarySpecification
{
    public AndSpecification( Iterable<Specification<Composite>> operands )
    {
        super(operands);
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        return Specifications.and( operands ).satisfiedBy( item );
    }

    @Override
    public String toString()
    {
        String str = "(";
        String and = "";
        for( Specification<Composite> operand : operands )
        {
            str += and+operand;
            and = " and ";
        }
        str+=")";

        return str;
    }
}
