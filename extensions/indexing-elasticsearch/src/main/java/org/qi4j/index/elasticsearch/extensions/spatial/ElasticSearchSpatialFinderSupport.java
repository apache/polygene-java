package org.qi4j.index.elasticsearch.extensions.spatial;

/*
 * Copyright 2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CircleBuilder;
import org.elasticsearch.common.geo.builders.PointBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.geoPolygonFilter;
import static org.elasticsearch.index.query.FilterBuilders.geoShapeFilter;

public final class ElasticSearchSpatialFinderSupport
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchSpatialFinderSupport.class);

    // http://spatialreference.org/ref/epsg/4327/

    private static final String SUPPORTED_SPATIAL_PROJECTION_NAME = "EPSG:4327";

    /* package */ static Object resolveVariable( Object value, Map<String, Object> variables )
    {
        if( value == null )
        {
            return null;
        }
        if( value instanceof Variable)
        {
            Variable var = (Variable) value;
            Object realValue = variables.get( var.variableName() );
            if( realValue == null )
            {
                throw new IllegalArgumentException( "Variable " + var.variableName() + " not bound" );
            }
            return realValue;
        }
        return value;
    }

//    /* package */ static interface ComplexTypeSupport
//    {
//
//        FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables );
//
//        FilterBuilder contains( ContainsSpecification<?> spec, Map<String, Object> variables );
//
//        FilterBuilder containsAll( ContainsAllSpecification<?> spec, Map<String, Object> variables );
//
//    }

    public interface ModuleHelper {
        void setModule(Module module);
    }

    public static interface SpatialQuerySpecSupport extends ModuleHelper
    {
        TGeometry processSpecification(FilterBuilder filterBuilder, Specification<?> spec, Map<String, Object> variables)  throws EntityFinderException;
       // void processSpatialPredicatesSpecification(FilterBuilder filterBuilder, SpatialPredicatesSpecification<?> spec, Map<String, Object> variables, Module module);
    }



    public static class SpatialSupport
                implements  SpatialQuerySpecSupport {





        Module module;

        public void setModule(Module module) {
            this.module = module;
        }





        public TGeometry processSpecification( FilterBuilder filterBuilder,
                                                   Specification<?> spec,
                                                    Map<String, Object> variables )
                throws EntityFinderException
        {
             // System.out.println("Processing Spatial stuff");

            if ( spec instanceof SpatialPredicatesSpecification) {
                SpatialPredicatesSpecification<?> spatialPredicatesSpecificationSpec = (SpatialPredicatesSpecification) spec;
                processSpatialPredicatesSpecification(filterBuilder, spatialPredicatesSpecificationSpec, variables, module);
//
            } else if ( spec instanceof SpatialConvertSpecification) {
                // System.out.println("SpatialConvertSpecification");
//
                SpatialConvertSpecification<?> spatialConvertSpec = (SpatialConvertSpecification) spec;
                processSpatialConvertSpecification(filterBuilder, spatialConvertSpec, variables);

            } else {

                throw new UnsupportedOperationException( "Spatial Query specification unsupported by Elastic Search "
                        + "(New Query API support missing?): "
                        + spec.getClass() + ": " + spec );
            }
            return null;
        }

        public void processSpatialPredicatesSpecification( FilterBuilder filterBuilder,
                                               SpatialPredicatesSpecification<?> spec,
                                               Map<String, Object> variables, Module module)
        {
            LOGGER.trace( "Processing processSpatialPredicatesSpecification {}", spec );
            String name = spec.property().toString();
            String value = toString( spec.value(), variables );

            if ( spec instanceof ST_WithinSpecification) {

                ST_WithinSpecification<?> withinSpec = ( ST_WithinSpecification ) spec;
                processWithinSpecification(filterBuilder, withinSpec, variables, module);

            } else {
                    throw new UnsupportedOperationException( "Spatial predicates specification unsupported by Elastic Search "
                        + "(New Query API support missing?): "
                        + spec.getClass() + ": " + spec );

                }
        }

        private void processWithinSpecification( FilterBuilder filterBuilder,
                                                 ST_WithinSpecification<?> spec,
                                                 Map<String, Object> variables, Module module ) {
            LOGGER.trace("Processing ST_WithinSpecification {}", spec);

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


        private GeoShapeFilterBuilder createShapePointFilter(String name, TPoint point) {


            PointBuilder pointBuilder = ShapeBuilder.newPoint(point.x(), point.y());

            CircleBuilder circleBuilder = ShapeBuilder.newCircleBuilder();
            circleBuilder.center(point.x(), point.y()).radius(10000, DistanceUnit.METERS);

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

        public void processSpatialConvertSpecification( FilterBuilder filterBuilder,
                                                           SpatialConvertSpecification<?> spec,
                                                           Map<String, Object> variables) {
            LOGGER.trace("Processing processSpatialConvertSpecification {}", spec);
            String name = spec.property().toString();
            // String value = toString(spec. value(), variables);


            if ( spec instanceof ST_GeomFromTextSpecification) {

                ST_GeomFromTextSpecification<?> geometryFromTextSpec = ( ST_GeomFromTextSpecification ) spec;
                processGeometryFromTextSpecification(filterBuilder, geometryFromTextSpec, variables);

            } else {
                throw new UnsupportedOperationException( "Spatial convert specification unsupported by Elastic Search "
                        + "(New Query API support missing?): "
                        + spec.getClass() + ": " + spec );

            }

        }


        private void processGeometryFromTextSpecification( FilterBuilder filterBuilder,
                                                 ST_GeomFromTextSpecification<?> spec,
                                                 Map<String, Object> variables ) {
            LOGGER.trace("Processing ST_GeomFromTextSpecification {}", spec);
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


        private String toString( Object value, Map<String, Object> variables )
        {
            if ( value == null ) {
                return null;
            }
            if ( value instanceof Variable) {
                Variable var = (Variable) value;
                Object realValue = variables.get( var.variableName() );
                if ( realValue == null ) {
                    throw new IllegalArgumentException( "Variable " + var.variableName() + " not bound" );
                }
                return realValue.toString();
            }
            return value.toString();
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




//    /* package */ static class MoneySupport
//            implements ComplexTypeSupport
//    {
//        private static final String CURRENCY = ".currency";
//        private static final String AMOUNT = ".amount";
//
//        @Override
//        public FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables )
//        {
////            String name = spec.property().toString();
////            String currencyTerm = name + CURRENCY;
////            String amountTerm = name + AMOUNT;
////            BigMoney money = ( (BigMoneyProvider) spec.value() ).toBigMoney();
////            String currency = money.getCurrencyUnit().getCurrencyCode();
////            BigDecimal amount = money.getAmount();
////            if( spec instanceof EqSpecification)
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        termFilter( amountTerm, amount )
////                );
////            }
////            else if( spec instanceof NeSpecification )
////            {
////                return andFilter(
////                        existsFilter( name ),
////                        orFilter( notFilter( termFilter( currencyTerm, currency ) ),
////                                notFilter( termFilter( amountTerm, amount ) ) )
////                );
////            }
////            else if( spec instanceof GeSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).gte( amount )
////                );
////            }
////            else if( spec instanceof GtSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).gt( amount )
////                );
////            }
////            else if( spec instanceof LeSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).lte( amount )
////                );
////            }
////            else if( spec instanceof LtSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).lt( amount )
////                );
////            }
////            else
////            {
////                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
////                        + "(New Query API support missing?): "
////                        + spec.getClass() + ": " + spec );
////            }
//        }
//
//        @Override
//        public FilterBuilder contains( ContainsSpecification<?> spec,
//                                       Map<String, Object> variables )
////        {
////            String name = spec.collectionProperty().toString();
////            BigMoney money = ( (BigMoneyProvider) spec.value() ).toBigMoney();
////            String currency = money.getCurrencyUnit().getCurrencyCode();
////            BigDecimal amount = money.getAmount();
////            return andFilter(
////                    termFilter( name + CURRENCY, currency ),
////                    termFilter( name + AMOUNT, amount )
////            );
//        }
//
//        // @Override
//        public FilterBuilder containsAll( ContainsAllSpecification<?> spec,
//                                          Map<String, Object> variables )
//        {
////            String name = spec.collectionProperty().toString();
////            AndFilterBuilder contAllFilter = new AndFilterBuilder();
////            for( Object value : spec.containedValues() )
////            {
////                BigMoney money = ( (BigMoneyProvider) value ).toBigMoney();
////                String currency = money.getCurrencyUnit().getCurrencyCode();
////                BigDecimal amount = money.getAmount();
////                contAllFilter.add( termFilter( name + CURRENCY, currency ) );
////                contAllFilter.add( termFilter( name + AMOUNT, amount ) );
////            }
////            return contAllFilter;
//            return null;
//        }


    private String toString( Object value, Map<String, Object> variables )
    {
        if ( value == null ) {
            return null;
        }
        if ( value instanceof Variable) {
            Variable var = (Variable) value;
            Object realValue = variables.get( var.variableName() );
            if ( realValue == null ) {
                throw new IllegalArgumentException( "Variable " + var.variableName() + " not bound" );
            }
            return realValue.toString();
        }
        return value.toString();
    }

    private ElasticSearchSpatialFinderSupport()
    {
    }

}
