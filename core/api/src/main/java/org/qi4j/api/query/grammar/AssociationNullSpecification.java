package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.association.Association;
import org.qi4j.api.composite.Composite;

/**
 * Association null Specification.
 */
public class AssociationNullSpecification<T>
    implements Predicate<Composite>
{
    private AssociationFunction<T> association;

    public AssociationNullSpecification( AssociationFunction<T> association )
    {
        this.association = association;
    }

    public AssociationFunction<T> association()
    {
        return association;
    }

    @Override
    public boolean test( Composite item )
    {
        try
        {
            Association<T> assoc = association.apply( item );

            if( assoc == null )
            {
                return true;
            }

            return assoc.get() == null;
        }
        catch( IllegalArgumentException e )
        {
            return true;
        }
    }

    @Override
    public String toString()
    {
        return association.toString() + "is null";
    }
}
