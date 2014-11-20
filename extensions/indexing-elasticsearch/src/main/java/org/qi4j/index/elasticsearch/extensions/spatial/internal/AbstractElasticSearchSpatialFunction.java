package org.qi4j.index.elasticsearch.extensions.spatial.internal;

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CircleBuilder;
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
import org.qi4j.index.elasticsearch.ElasticSearchFinder;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinderSupport;

import java.util.Map;

/**
 * Created by jj on 20.11.14.
 */
public abstract class AbstractElasticSearchSpatialFunction {


    protected Module module;

    public void setModule(Module module) {
        this.module = module;
    }

    protected void addFilter( FilterBuilder filter, FilterBuilder into )
    {
        if ( into instanceof AndFilterBuilder) {
            ( (AndFilterBuilder) into ).add( filter );
        } else if ( into instanceof OrFilterBuilder) {
            ( (OrFilterBuilder) into ).add( filter );
        } else {
            throw new UnsupportedOperationException( "FilterBuilder is nor an AndFB nor an OrFB, cannot continue." );
        }
    }


    protected GeoShapeFilterBuilder createShapeFilter(String name, TGeometry geometry, ShapeRelation relation )
    {
        if (geometry instanceof TPoint)
        {
            CircleBuilder circleBuilder = ShapeBuilder.newCircleBuilder();
            circleBuilder.center(((TPoint) geometry).X(), ((TPoint)geometry).Y()).radius(10000, DistanceUnit.METERS);
            return FilterBuilders.geoShapeFilter(name, circleBuilder, relation);
        }
        else if (geometry instanceof TPolygon)
        {
            PolygonBuilder polygonBuilder = ShapeBuilder.newPolygon();

            for (int i = 0; i < ((TPolygon) geometry).shell().get().points().get().size(); i++) {
                TPoint point = ((TPolygon) geometry).shell().get().getPointN(i);
                System.out.println(point);

                polygonBuilder.point(
                        point.coordinates().get().get(0).getOrdinate(Coordinate.X),
                        point.coordinates().get().get(1).getOrdinate(Coordinate.X)
                );
            }

            return  FilterBuilders.geoShapeFilter(name, polygonBuilder, relation);
        }
        else
        {

        }

        return null;
    }


    protected TGeometry resolveGeometry(Specification<Composite> spec, Module module) throws Exception
    {

        if (spec instanceof SpatialPredicatesSpecification)
        {
            if (((SpatialPredicatesSpecification)spec).value() != null)
            {
                return (TGeometry)((SpatialPredicatesSpecification)spec).value();
            }
            else if (((SpatialPredicatesSpecification)spec).operator() != null)
            {

                if (((SpatialPredicatesSpecification) spec).operator() instanceof SpatialConvertSpecification)
                {
                    return ((SpatialConvertSpecification) ((SpatialPredicatesSpecification) spec).operator()).convert(module);
                }

                return null;
            }
        }

        return null;
    }





}
