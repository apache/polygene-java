package org.qi4j.api.query.grammar.extensions.spatial.predicate;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.functional.Specification;

/**
 * ST_Within Specification.
 */
public class ST_WithinSpecification<T extends TGeometry>
    extends SpatialPredicatesSpecification<T>
{

    private double distance;
    private TUnit unit;

    public ST_WithinSpecification(PropertyFunction<T> property, TGeometry value)
    {
        super( property, value );
    }

    public ST_WithinSpecification(PropertyFunction<T> property, TPoint value, double distance, TUnit unit)
    {
        super( property, value );
        this.distance   = distance;
        this.unit       = unit;
    }

    public ST_WithinSpecification(PropertyFunction<T> property, Specification<SpatialConvertSpecification> operator, double distance, TUnit unit)
    {
        super( property, operator );
        this.distance   = distance;
        this.unit       = unit;
    }

    public ST_WithinSpecification(PropertyFunction<T> property, Specification<SpatialConvertSpecification> operator, Variable variable)
    {
        super( property, operator );
    }

    public ST_WithinSpecification(PropertyFunction<T> property, Specification<SpatialConvertSpecification> operator)
    {
        super( property, operator );
    }

    public double getDistance() { return distance; }
    public TUnit  getUnit() { return unit; }


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
