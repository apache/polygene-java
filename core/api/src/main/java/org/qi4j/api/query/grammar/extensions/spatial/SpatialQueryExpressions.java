package org.qi4j.api.query.grammar.extensions.spatial;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.internal.HasShape;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.functional.Specification;

/**
 * Created by jakes on 2/8/14.
 */
public final class SpatialQueryExpressions extends QueryExpressions {

    // valid for TPoint values
    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within( Property<T> geometry1, TPoint value, double distance, TUnit unit) // T value )
    {
        return new ST_WithinSpecification( property(geometry1), value, distance, unit);
    }

    public static <T extends TGeometry>  ST_WithinSpecification<TGeometry> ST_Within( Property<T> geometry1, HasShape value)
    {
       return new ST_WithinSpecification( property(geometry1), value);
    }

    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within( Property<T> geometry1, Specification<SpatialConvertSpecification> operator, double distance, TUnit unit )
    {
        return new ST_WithinSpecification( property(geometry1), operator, distance, unit);
    }
/**
    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within( Property<T> geometry1, Specification<SpatialConvertSpecification> operator)
    {
        return new ST_WithinSpecification( property(geometry1), operator;
    }
*/
    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within( Property<T> geometry1, Specification<SpatialConvertSpecification> operator)
    {
        return new ST_WithinSpecification( property(geometry1), operator);
    }



    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within(  Specification<SpatialConvertSpecification> operator) // T value )
    {
        // return new ST_WithinSpecification<TGeometry>( property(geometry1), operator);
        return null;
    }


    public static <T extends TGeometry> ST_DisjointSpecification<TGeometry> ST_Disjoin( Property<T> geometry1, Specification<SpatialConvertSpecification> operator, long distance )
    {
        return new ST_DisjointSpecification( property(geometry1), operator, distance);
    }

    /**
    public static <TGeometry> ST_GeomFromTextSpecification<TGeometry> ST_GeomFromText( Property<TGeometry> geometry1, Specification<SpatialConvertSpecification> operator) // T value )
    {
        return new ST_GeomFromTextSpecification<TGeometry>( property(geometry1), operator);
    }
     */


   //  PropertyFunction<T> property

//    public static <TGeometry> ST_PointFromTextSpecification<TGeometry> ST_PointFromText( String WKT, int a)
//    {
//        // return null;
//        return new ST_PointFromTextSpecification<TGeometry>( WKT, a);
//    }

//    public static TGeomPoint ST_PointFromText( String WKT, int a) throws ParseException
//    {
//        // return null;
//        return (TGeomPoint)new ST_PointFromTextSpecification<TGeometry>( WKT, a).convert();
//        // return null;
//    }

    public static Specification<SpatialConvertSpecification> ST_GeometryFromText(String WKT, int srid)
    {
        //  return new LtSpecification<>( property( property ), value );
        return new ST_GeomFromTextSpecification( WKT, srid);
    }

    public static Specification<SpatialConvertSpecification> ST_GeometryFromText(String WKT, String crs)
    {
        //  return new LtSpecification<>( property( property ), value );
        return new ST_GeomFromTextSpecification( WKT, crs);
    }

    public static Specification<Composite> ST_GeometryFromText(String WKT, int srid, String foo)
    {
        //  return new LtSpecification<>( property( property ), value );
        return new ST_GeomFromTextSpecification( WKT, srid);
    }

    /**
    public static  ST_GeomFromTextSpecification<TGeometry> ST_GeomFromText(String WKT, int a) throws ParseException
    {
        // return null;
        return new ST_GeomFromTextSpecification<TGeometry>( WKT, a);
        // return null;
    }
     */



}
