package org.qi4j.api.query.grammar.extensions.spatial.convert;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.ExpressionSpecification;
import org.qi4j.api.structure.Module;

import java.text.ParseException;

/**
 * Base Spatial Predicates Specification.
 *
 * ST_Equals, ST_Disjoint, ST_Intersects, ST_Touches, ST_Crosses, ST_Within, ST_Contains, ST_Overlaps and ST_Relate
 */
public abstract class SpatialConvertSpecification<T>
    extends ExpressionSpecification
{
    protected  String WKT;
    protected  int srid;
    protected  String crs;

    public SpatialConvertSpecification(String WKT, int srid)
    {
        this.WKT = WKT;
        this.srid = srid;
    }

    public SpatialConvertSpecification(String WKT, String crs)
    {
        this.WKT = WKT;
        this.crs = crs;
    }



    public String property()
    {
        return WKT;
    }

    public String getCrs()
    {
        return crs;
    }

    @Override
    public final boolean satisfiedBy( Composite item )
    {
//        try
//        {
//            Property<TGeometry> prop = WKT.map( item );
//
//            if( prop == null )
//            {
//                return false;
//            }
//
//            TGeometry propValue = prop.get();
//            if( propValue == null )
//            {
//                return false;
//            }
//
//            return convert( propValue );
//        }
//        catch( IllegalArgumentException e )
//        {
//            return false;
//        }
        return true;
    }

    public abstract TGeometry convert(Module module) throws ParseException;

//    public TGeometry value()
//    {
//        return value;
//    }
}
