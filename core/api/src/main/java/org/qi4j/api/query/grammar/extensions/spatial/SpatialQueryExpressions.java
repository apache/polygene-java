package org.qi4j.api.query.grammar.extensions.spatial;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TShape;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_IntersectsSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.functional.Specification;


public final class SpatialQueryExpressions extends QueryExpressions
{

    // ST_Within
    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within(Property<T> geometry, TPoint param, double distance, TUnit unit)
    {
        return new ST_WithinSpecification(property(geometry), param, distance, unit);
    }

    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within(Property<T> geometry, TShape param)
    {
        return new ST_WithinSpecification(property(geometry), param);
    }

    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within(Property<T> geometry, Specification<SpatialConvertSpecification> operator, double distance, TUnit unit)
    {
        return new ST_WithinSpecification(property(geometry), operator, distance, unit);
    }

    public static <T extends TGeometry> ST_WithinSpecification<TGeometry> ST_Within(Property<T> geometry, Specification<SpatialConvertSpecification> operator)
    {
        return new ST_WithinSpecification(property(geometry), operator);
    }

    // ST_Disjoint
    public static <T extends TGeometry> ST_DisjointSpecification<TGeometry> ST_Disjoint(Property<T> geometry, Specification<SpatialConvertSpecification> operator, long distance)
    {
        return new ST_DisjointSpecification(property(geometry), operator, distance);
    }

    public static <T extends TGeometry> ST_DisjointSpecification<TGeometry> ST_Disjoint(Property<T> geometry, TPoint param, double distance, TUnit unit)
    {
        return new ST_DisjointSpecification(property(geometry), param, distance, unit);
    }

    public static <T extends TGeometry> ST_DisjointSpecification<TGeometry> ST_Disjoint(Property<T> geometry, TShape param)
    {
        return new ST_DisjointSpecification(property(geometry), param);
    }


    // ST_Intersects
    public static <T extends TGeometry> ST_IntersectsSpecification<TGeometry> ST_Intersects(Property<T> geometry, Specification<SpatialConvertSpecification> operator, long distance)
    {
        return new ST_IntersectsSpecification(property(geometry), operator, distance);
    }

    public static <T extends TGeometry> ST_IntersectsSpecification<TGeometry> ST_Intersects(Property<T> geometry, TPoint value, double distance, TUnit unit)
    {
        return new ST_IntersectsSpecification(property(geometry), value, distance, unit);
    }

    public static <T extends TGeometry> ST_IntersectsSpecification<TGeometry> ST_Intersects(Property<T> geometry, TShape param)
    {
        return new ST_IntersectsSpecification(property(geometry), param);
    }


    // ST_GeometryFromText
    public static Specification<SpatialConvertSpecification> ST_GeometryFromText(String WKT)
    {
        return ST_GeometryFromText(WKT, null);
    }

    public static Specification<SpatialConvertSpecification> ST_GeometryFromText(String WKT, String crs)
    {
        return new ST_GeomFromTextSpecification(WKT, crs);
    }

}
