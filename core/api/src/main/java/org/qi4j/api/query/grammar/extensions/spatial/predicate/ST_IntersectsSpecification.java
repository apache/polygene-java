package org.qi4j.api.query.grammar.extensions.spatial.predicate;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.functional.Specification;


public class ST_IntersectsSpecification<T extends TGeometry>
        extends SpatialPredicatesSpecification<T>
{

    private double distance;
    private TUnit unit;

    public ST_IntersectsSpecification(PropertyFunction<T> property, TGeometry param)
    {
        super(property, param);
    }

    public ST_IntersectsSpecification(PropertyFunction<T> property, Specification<SpatialConvertSpecification> operator, long distance)
    {
        super(property, operator);
    }

    public ST_IntersectsSpecification(PropertyFunction<T> property, TPoint param, double distance, TUnit unit)
    {
        super(property, param);
        this.distance = distance;
        this.unit = unit;
    }


    public double getDistance()
    {
        return distance;
    }

    public TUnit getUnit()
    {
        return unit;
    }


    @Override
    protected boolean compare(TGeometry param)
    {
        return param.equals(this.param);
    }

    @Override
    public String toString()
    {
        StringBuffer spec = new StringBuffer();
        spec.append("ST_INTERSECTS").append("( ").append(property.toString()).append(" INTERSECTS ");
        spec.append(param.toString());

        if (distance > 0)
        {
            spec.append(" WITH RADIUS ").append(distance).append(" ").append(unit);
        }

        spec.append(" ) ");
        return spec.toString();
    }
}
