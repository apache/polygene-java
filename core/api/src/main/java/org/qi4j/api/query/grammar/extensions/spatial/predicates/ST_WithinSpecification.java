package org.qi4j.api.query.grammar.extensions.spatial.predicates;

import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.functional.Specification;

/**
 * ST_Within Specification.
 */
public class ST_WithinSpecification<TGeometry>
    extends SpatialPredicatesSpecification<TGeometry>
{

    public ST_WithinSpecification(PropertyFunction<TGeometry> property, TGeometry value)
    {
        super( property, value );
    }

    public ST_WithinSpecification(PropertyFunction<TGeometry> property, Specification<SpatialConvertSpecification> operator, long distance)
    {
        super( property, operator );
    }

    @Override
    protected boolean compare( TGeometry value )
    {
        return value.equals( this.value );
    }

    @Override
    public String toString()
    {
        return "ST_WithinSpecification"; // property.toString() + " is within " + value.toString();
    }
}
