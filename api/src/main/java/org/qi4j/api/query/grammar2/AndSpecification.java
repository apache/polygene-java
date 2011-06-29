package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.util.Iterables;

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
