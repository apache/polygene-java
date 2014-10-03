package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specifications;

/**
 * OR Specification.
 */
public class OrSpecification
    extends BinarySpecification
{

    public OrSpecification( Iterable<Predicate<Composite>> operands )
    {
        super( operands );
    }

    @Override
    public boolean test( Composite item )
    {
        return Specifications.or( operands ).test( item );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "(" );
        String or = "";
        for( Predicate<Composite> operand : operands )
        {
            sb.append( or ).append( operand );
            or = " or ";
        }
        return sb.append( ")" ).toString();
    }

}
