package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

/**
 * OR Specification.
 */
public class OrSpecification
    extends BinarySpecification
{

    public OrSpecification( Iterable<Specification<Composite>> operands )
    {
        super( operands );
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        return Specifications.or( operands ).satisfiedBy( item );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "(" );
        String or = "";
        for( Specification<Composite> operand : operands )
        {
            sb.append( or ).append( operand );
            or = " or ";
        }
        return sb.append( ")" ).toString();
    }

}
