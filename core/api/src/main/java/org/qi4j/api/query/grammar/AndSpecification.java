package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specifications;

/**
 * AND Specification.
 */
public class AndSpecification
    extends BinarySpecification
{

    public AndSpecification( Iterable<Predicate<Composite>> operands )
    {
        super( operands );
    }

    @Override
    public boolean test( Composite item )
    {
        return Specifications.and( operands ).test( item );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "(" );
        String and = "";
        for( Predicate<Composite> operand : operands )
        {
            sb.append( and ).append( operand );
            and = " and ";
        }
        return sb.append( ")" ).toString();
    }

}
