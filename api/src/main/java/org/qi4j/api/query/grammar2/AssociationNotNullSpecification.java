package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.association.Association;

/**
 * TODO
 */
public class AssociationNotNullSpecification<T>
    extends ExpressionSpecification
{
    AssociationFunction<T> association;

    public AssociationNotNullSpecification( AssociationFunction<T> association )
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
                return false;

            return assoc.get() != null;
        } catch( IllegalArgumentException e )
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return association.toString()+ "is not null";
    }
}
