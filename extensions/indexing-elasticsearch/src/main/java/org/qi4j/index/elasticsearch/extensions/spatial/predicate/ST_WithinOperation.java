package org.qi4j.index.elasticsearch.extensions.spatial.predicate;

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CircleBuilder;
import org.elasticsearch.common.geo.builders.PointBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.geoPolygonFilter;

/**
 * Created by jj on 19.11.14.
 */
public class ST_WithinOperation implements ElasticSearchSpatialPredicateFinderSupport.PredicateSpecification {

    Module module;

    public void setModule(Module module) {
        this.module = module;
    }

    public void processSpecification( FilterBuilder filterBuilder,
                                      SpatialPredicatesSpecification<?> spec,
                                      Map<String, Object> variables )
            throws EntityFinderException
    {
        if (spec.value() == null && spec.operator() == null)
            throw new UnsupportedOperationException("ST_Within specification ...todo :"
                    + spec.getClass() + ": " + spec);

        // addFilter( existsFilter( spec.property().toString() ), filterBuilder );
        // if ((ST_WithinSpecification)spec.operator() instanceof SpatialConvertSpecification )
        String name = spec.property().toString();
        // String value = toString(spec.value(), variables);

        TGeometry geometry = null;

        if (spec.operator() != null) {
            try {
                geometry = resolveGeometry(spec, module);
            } catch (Exception _ex) {
                _ex.printStackTrace();
            }
        } else {
            geometry = (TGeometry)spec.value();
        }
        // // System.out.println(geometry + "Geometry Type Name : " + geometry.getClass().getName());

        if (geometry instanceof TPoint) {
            // System.out.println("Its a point " + ((TPoint)geometry).coordinates() + " name "  + name);

            // // System.out.println("Lat " + ((TPoint) spec.value()).coordinates().get().get(0));

            GeoShapeFilterBuilder geoShapeFilterBuilder  = createShapePointFilter(name, (TPoint) geometry);
            addFilter(geoShapeFilterBuilder, filterBuilder);


            /**
             addFilter(geoDistanceFilter(name)

             .lat(((TPoint) geometry).coordinates().get().get(0).getOrdinate(Coordinate.X))
             .lon(((TPoint) geometry).coordinates().get().get(1).getOrdinate(Coordinate.X))
             .distance(100, DistanceUnit.KILOMETERS)
             .geoDistance(GeoDistance.ARC),


             filterBuilder);
             */

        } else
        if (geometry instanceof TPolygon) {
            System.out.println("## IS POLYGON ");

            GeoShapeFilterBuilder geoShapeFilterBuilder = createShapeFilter(name, (TPolygon) geometry);

            GeoPolygonFilterBuilder polygonFilterBuilder = geoPolygonFilter(name);
            //

            System.out.println("Size of Points " + ((TPolygon) geometry).shell().get().points().get().size());

            for (int i = 0; i < ((TPolygon) geometry).shell().get().points().get().size(); i++) {
                TPoint point = ((TPolygon) geometry).shell().get().getPointN(i);
                System.out.println(point);
                polygonFilterBuilder.addPoint(
                        //  point.coordinates().get().get(0).getOrdinate(Coordinate.X),
                        // point.coordinates().get().get(1).getOrdinate(Coordinate.X)

                        point.coordinates().get().get(0).getOrdinate(Coordinate.X),
                        point.coordinates().get().get(1).getOrdinate(Coordinate.X)
                );
            }

            // addFilter(polygonFilterBuilder, filterBuilder);
            addFilter(geoShapeFilterBuilder, filterBuilder);

            //       ((TPolygon) geometry).shell().get().getPointN(i)
            /**
             addFilter(geoPolygonFilter(name)
             .addPoint(0, 0)
             .addPoint(0, 0)

             .lat(((TPoint) geometry).coordinates().get().get(0).getOrdinate(Coordinate.X))
             .lon(((TPoint) geometry).coordinates().get().get(1).getOrdinate(Coordinate.X))
             .distance(100, DistanceUnit.KILOMETERS)
             .geoDistance(GeoDistance.ARC),


             filterBuilder);
             */
        }

        if (geometry instanceof TGeometry) {
            // http://blog.sallarp.com/geojson-google-maps-editor.html
            // TODO Use polygon or Multipolygon
        }

//            String property = "foo";
//            Double lat =  3.138722;  // 3.138722;// Double.parseDouble(query.nextToken());
//            Double lon =  101.386849; // Double.parseDouble(query.nextToken());
//            Integer radius = 1000; // Integer.parseInt(query.nextToken());
//
//            TGeomPoint point = ((TGeomPoint)((ST_WithinSpecification) spec).value());
//            // point.coordinates().get().get(0)
//
//            addFilter(
//
//                    geoDistanceFilter(name)
//
//                            .lat(((TGeomPoint) spec.value()).coordinates().get().get(0))
//                            .lon(((TGeomPoint) spec.value()).coordinates().get().get(1))
//                            .distance(radius, DistanceUnit.KILOMETERS)
//                            .geoDistance(GeoDistance.ARC),
//
//
//                    filterBuilder);

    }


    private TGeometry resolveGeometry(Specification<Composite> spec, Module module) throws Exception
    {

        if (spec instanceof  SpatialPredicatesSpecification)
        {
            if (((SpatialPredicatesSpecification)spec).value() != null)
            {
                return (TGeometry)((SpatialPredicatesSpecification)spec).value();
            }
            else if (((SpatialPredicatesSpecification)spec).operator() != null)
            {
                // System.out.println("SpatialConvertSpecification Operator");
                // JJ TODO resolve Conversion Operator

                if (((SpatialPredicatesSpecification) spec).operator() instanceof SpatialConvertSpecification)
                {
                    // if (((SpatialPredicatesSpecification) spec).operator() instan)
                    // System.out.println("### " + ((SpatialConvertSpecification) ((SpatialPredicatesSpecification) spec).operator()).property());

                    return ((SpatialConvertSpecification) ((SpatialPredicatesSpecification) spec).operator()).convert(module);
                }

                return null;
            }
        }

        return null;
    }


    private GeoShapeFilterBuilder createShapePointFilter(String name, TPoint point) {


        PointBuilder pointBuilder = ShapeBuilder.newPoint(point.X(), point.Y());

        CircleBuilder circleBuilder = ShapeBuilder.newCircleBuilder();
        circleBuilder.center(point.X(), point.Y()).radius(10000, DistanceUnit.METERS);

        // ShapeBuilder shapeBuilder = ShapeBuilder.newPolygon().point(99.0, -1.0).point(99.0, 3.0).point(103.0, 3.0).point(103.0, -1.0).point(99.0, -1.0);
        // GeoShapeFilterBuilder shapeFilterBuilder = geoShapeFilter(name);
/**
 for (int i = 0; i < ((TPolygon) polygon).shell().get().points().get().size(); i++) {
 TPoint point = ((TPolygon) polygon).shell().get().getPointN(i);
 System.out.println(point);

 polygonBuilder.point(
 point.coordinates().get().get(0).getOrdinate(Coordinate.X),
 point.coordinates().get().get(1).getOrdinate(Coordinate.X)
 );
 }
 */

        GeoShapeFilterBuilder filter = FilterBuilders.geoShapeFilter(name, circleBuilder, ShapeRelation.WITHIN);

        return filter;
    }


    private GeoShapeFilterBuilder createShapeFilter(String name, TPolygon polygon) {


        PolygonBuilder polygonBuilder = ShapeBuilder.newPolygon();

        ShapeBuilder shapeBuilder = ShapeBuilder.newPolygon().point(99.0, -1.0).point(99.0, 3.0).point(103.0, 3.0).point(103.0, -1.0).point(99.0, -1.0);
        // GeoShapeFilterBuilder shapeFilterBuilder = geoShapeFilter(name);

        for (int i = 0; i < ((TPolygon) polygon).shell().get().points().get().size(); i++) {
            TPoint point = ((TPolygon) polygon).shell().get().getPointN(i);
            System.out.println(point);

            polygonBuilder.point(
                    point.coordinates().get().get(0).getOrdinate(Coordinate.X),
                    point.coordinates().get().get(1).getOrdinate(Coordinate.X)
            );
        }


        GeoShapeFilterBuilder filter = FilterBuilders.geoShapeFilter(name, polygonBuilder, ShapeRelation.WITHIN);

        return filter;
    }

    private void addFilter( FilterBuilder filter, FilterBuilder into )
    {
        if ( into instanceof AndFilterBuilder) {
            ( (AndFilterBuilder) into ).add( filter );
        } else if ( into instanceof OrFilterBuilder) {
            ( (OrFilterBuilder) into ).add( filter );
        } else {
            throw new UnsupportedOperationException( "FilterBuilder is nor an AndFB nor an OrFB, cannot continue." );
        }
    }

}
