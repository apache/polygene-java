package org.qi4j.api.query.grammar.extensions.spatial.predicate;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.functional.Specification;

public class ST_WithinSpecification<T extends TGeometry>
    extends SpatialPredicatesSpecification<T>
{

    private double distance;
    private TUnit unit;

    public ST_WithinSpecification( PropertyFunction<T> property, TGeometry param )
    {
        super( property, param );
    }

    public ST_WithinSpecification( PropertyFunction<T> property, TPoint param, double distance, TUnit unit )
    {
        super( property, param );
        this.distance = distance;
        this.unit = unit;
    }

    public ST_WithinSpecification( PropertyFunction<T> property,
                                   Specification<SpatialConvertSpecification> operator,
                                   double distance,
                                   TUnit unit
    )
    {
        super( property, operator );
        this.distance = distance;
        this.unit = unit;
    }

    public ST_WithinSpecification( PropertyFunction<T> property, Specification<SpatialConvertSpecification> operator )
    {
        super( property, operator );
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
    protected boolean compare( TGeometry param )
    {
        return param.equals( this.param );
    }

    @Override
    public String toString()
    {
        StringBuilder spec = new StringBuilder();
        spec.append( "ST_WITHIN" );
        spec.append( "( " );
        spec.append( property.toString() );
        spec.append( " IS WITHIN " );
        spec.append( param.toString() );

        if( distance > 0 )
        {
            spec.append( " WITH RADIUS " );
            spec.append( distance );
            spec.append( " " );
            spec.append( unit );
        }

        spec.append( " ) " );
        return spec.toString();
    }
}
