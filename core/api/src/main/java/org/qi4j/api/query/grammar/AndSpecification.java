package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

/**
 * AND Specification.
 */
public class AndSpecification
    extends BinarySpecification
{

    public AndSpecification( Iterable<Specification<Composite>> operands )
    {
        super( operands );
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        return Specifications.and( operands ).satisfiedBy( item );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "(" );
        String and = "";
        for( Specification<Composite> operand : operands )
        {
            sb.append( and ).append( operand );
            and = " and ";
        }
        return sb.append( ")" ).toString();
    }

}
