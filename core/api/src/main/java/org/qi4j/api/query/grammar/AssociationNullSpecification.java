package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.association.Association;

/**
 * TODO
 */
public class AssociationNullSpecification<T>
        extends ExpressionSpecification
{
    AssociationFunction<T> association;

    public AssociationNullSpecification( AssociationFunction<T> association )
    {
        this.association = association;
    }

    public AssociationFunction<T> getAssociation()
    {
        return association;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        try
        {
            Association<T> assoc = association.map( item );

            if (assoc == null)
                return true;

            return assoc.get() == null;
        } catch( IllegalArgumentException e )
        {
            return true;
        }
    }

    @Override
    public String toString()
    {
        return association.toString()+ "is null";
    }
}
