package org.qi4j.api.query.grammar;

import org.qi4j.api.association.Association;
import org.qi4j.api.composite.Composite;

/**
 * Association not null Specification.
 */
public class AssociationNotNullSpecification<T>
    extends ExpressionSpecification
{
    private AssociationFunction<T> association;

    public AssociationNotNullSpecification( AssociationFunction<T> association )
    {
        this.association = association;
    }

    public AssociationFunction<T> association()
    {
        return association;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        try
        {
            Association<T> assoc = association.map( item );

            if( assoc == null )
            {
                return false;
            }

            return assoc.get() != null;
        }
        catch( IllegalArgumentException e )
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return association.toString() + "is not null";
    }
}
