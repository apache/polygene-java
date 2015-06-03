package org.qi4j.api.query.grammar.extensions.spatial.predicate;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.functional.Specification;

public class ST_DisjointSpecification<T extends TGeometry>
    extends SpatialPredicatesSpecification<T>
{

    private double distance;
    private TUnit unit;

    public ST_DisjointSpecification( PropertyFunction<T> property, TGeometry param )
    {
        super( property, param );
    }

    // TODO: [niclas] 'distance' is not used...??? Missing tests?
    public ST_DisjointSpecification( PropertyFunction<T> property,
                                     Specification<SpatialConvertSpecification> operator,
                                     long distance
    )
    {
        super( property, operator );
    }

    public ST_DisjointSpecification( PropertyFunction<T> property, TPoint value, double distance, TUnit unit )
    {
        super( property, value );
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
    protected boolean compare( TGeometry param )
    {
        return param.equals( this.param );
    }

    @Override
    public String toString()
    {
        StringBuilder spec = new StringBuilder();
        spec.append( "ST_DISJOINT" );
        spec.append( "( " );
        spec.append( property.toString() );
        spec.append( " IS NOT WITHIN " );
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
