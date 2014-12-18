package org.qi4j.api.query.grammar.extensions.spatial.predicate;

import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.PropertyFunction;

/**
 * ST_Within Specification.
 *
 *
 */
public class ST_ContainsSpecification<T extends TGeometry>
    extends SpatialPredicatesSpecification<TGeometry>
{
    public ST_ContainsSpecification(PropertyFunction<TGeometry> property, TGeometry value)
    {
        super( property, value );
    }

    @Override
    protected boolean compare( TGeometry value )
    {
        return value.equals( this.value );
    }

    @Override
    public String toString()
    {
        return property.toString() + " is within " + value.toString();
    }
}
