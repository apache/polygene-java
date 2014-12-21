package org.qi4j.index.elasticsearch.extensions.spatial.internal;

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CircleBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchFinder;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder;
import org.qi4j.library.spatial.v2.projections.ProjectionsRegistry;
import org.qi4j.spi.query.EntityFinderException;

import java.lang.reflect.Type;
import java.util.Map;

import static org.qi4j.library.spatial.v2.transformations.TTransformations.Transform;
import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.SpatialIndexMapper.IndexMappingCache;


/**
 * Created by jj on 20.11.14.
 */
public abstract class AbstractElasticSearchSpatialFunction {

    private static final String EPSG_4326 = "EPSG:4326";
    private static final String DefaultProjection = EPSG_4326;
    private static final double DefaultProjectionConversionPrecisionInMeters = 2.00;

    protected Module module;
    protected ElasticSearchSupport support;

    public void setModule(Module module, ElasticSearchSupport support)
    {
        this.module = module;
        this.support = support;
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

    protected TGeometry verifyProjection(TGeometry tGeometry)
    {
        if (new ProjectionsRegistry().getCRS(tGeometry.getCRS()) == null)
            throw new RuntimeException("Project with the CRS Identity " + tGeometry.getCRS() + " is unknown. Supported projections are JJ TODO" );


        try {
            Transform(module).from(tGeometry).to(DefaultProjection, DefaultProjectionConversionPrecisionInMeters);
        } catch (Exception _ex)
        {
            _ex.printStackTrace();
        }

        return tGeometry; // ATTENTION - we are transforming as per "Reference"
    }

    protected boolean isPropertyOfType(Class type, PropertyFunction propertyFunction)
    {
        Type returnType = Classes.typeOf(propertyFunction.accessor());
        Type propertyTypeAsType = GenericPropertyInfo.toPropertyType(returnType);


        System.out.println("---- > " + propertyTypeAsType.getTypeName());

        Class clazz;

        try
        {
            clazz = Class.forName(propertyTypeAsType.getTypeName());
        } catch(Exception _ex)
        {
            throw new RuntimeException(_ex);
        }
        // if (clazz instanceof TGeometry)

        if (type.isAssignableFrom(clazz))
            return true;
        else
            return false;
    }

    protected boolean isPropertyOfTypeTPoint(PropertyFunction propertyFunction)
    {
        // String typeName = Classes.typeOf(propertyFunction.accessor()).getTypeName();
        // System.out.println(typeName);

        Type returnType = Classes.typeOf(propertyFunction.accessor());
        Type propertyTypeAsType = GenericPropertyInfo.toPropertyType(returnType);


        System.out.println("---- > " + propertyTypeAsType.getTypeName());

        Class clazz;

        try
        {
            clazz = Class.forName(propertyTypeAsType.getTypeName());
        } catch(Exception _ex)
        {
            throw new RuntimeException(_ex);
        }
        // if (clazz instanceof TGeometry)

        if (TPoint.class.isAssignableFrom(clazz))
            return true;
        else
            return false;
    }

    protected boolean isMappedAsGeoPoint(PropertyFunction property)
    {
        // return Mappings(support).onIndex(support.index()).andType(support.entitiesType()).isGeoPoint(property.toString());
        return IndexMappingCache.isMappedAsGeoPoint(support.index(), support.entitiesType(), property.toString());
    }

    protected boolean isMappedAsGeoShape(PropertyFunction property)
    {
        // return Mappings(support).onIndex(support.index()).andType(support.entitiesType()).isGeoShape(property.toString());
        return IndexMappingCache.isMappedAsGeoShape(support.index(), support.entitiesType(), property.toString());
    }

    protected boolean isMapped(PropertyFunction property)
    {
        return IndexMappingCache.mappingExists(support.index(), support.entitiesType(), property.toString());
    }

    protected boolean isSpatial(PropertyFunction property)
    {
        return false;
    }



    protected GeoShapeFilterBuilder createShapeFilter(String name, TPoint point, ShapeRelation relation, double distance, TUnit unit)
    {
        return createShapeFilter(name, point, relation, distance, convertDistanceUnit(unit));
    }

    protected GeoShapeFilterBuilder createShapeFilter(String name, TGeometry geometry, ShapeRelation relation)
    {
        return createShapeFilter(name, geometry, relation, 0, null);
    }

    protected GeoDistanceFilterBuilder createGeoDistanceFilter(String name, TPoint tPoint, double distance, TUnit unit)
    {
        // Lat = Y Long = X
      return  FilterBuilders.geoDistanceFilter(name)
              .lat(tPoint.y())
              .lon(tPoint.x())
              .distance(distance, convertDistanceUnit(unit));
    }


    private DistanceUnit convertDistanceUnit(TUnit tUnit)
    {
        switch (tUnit)
        {
            case MILLIMETER : return DistanceUnit.MILLIMETERS;
            case CENTIMETER : return DistanceUnit.CENTIMETERS;
            case METER      : return DistanceUnit.METERS;
            case KILOMETER  :return DistanceUnit.KILOMETERS;
            default : throw new RuntimeException("Can not convert Units");
        }
    }


    private GeoShapeFilterBuilder createShapeFilter(String name, TGeometry geometry, ShapeRelation relation, double distance, DistanceUnit unit  )
    {
        if (geometry instanceof TPoint)
        {
            CircleBuilder circleBuilder = ShapeBuilder.newCircleBuilder();
            circleBuilder.center(((TPoint) geometry).x(), ((TPoint)geometry).y()).radius(distance, unit);
            return FilterBuilders.geoShapeFilter(name, circleBuilder, relation);
        }
        else if (geometry instanceof TPolygon)
        {
            PolygonBuilder polygonBuilder = ShapeBuilder.newPolygon();

            for (int i = 0; i < ((TPolygon) geometry).shell().get().points().get().size(); i++) {
                TPoint point = ((TPolygon) geometry).shell().get().getPointN(i);
                System.out.println(point);

                polygonBuilder.point(
                        point.x(), point.y()
                );
            }

            return  FilterBuilders.geoShapeFilter(name, polygonBuilder, relation);
        }
        else
        {

        }

        return null;
    }


    protected TGeometry resolveGeometry( FilterBuilder filterBuilder, Specification<Composite> spec, Module module) throws  EntityFinderException
    {

        if (spec instanceof SpatialPredicatesSpecification)
        {
            if (((SpatialPredicatesSpecification)spec).value() != null)
            {
                return ((SpatialPredicatesSpecification)spec).value();
            }
            else if (((SpatialPredicatesSpecification)spec).operator() != null)
            {

                if (((SpatialPredicatesSpecification) spec).operator() instanceof SpatialConvertSpecification)
                {
                    executeSpecification(filterBuilder, (SpatialPredicatesSpecification)spec, null);
                    System.out.println("Converted Geometry " + ((SpatialConvertSpecification) ((SpatialPredicatesSpecification) spec).operator()).getGeometry());
                    return ((SpatialConvertSpecification) ((SpatialPredicatesSpecification) spec).operator()).getGeometry();
                    // return executeSpecification(filterBuilder, (SpatialPredicatesSpecification)spec, null);
                }

                return null;
            }
        }

        return null;
    }

    private void executeSpecification( FilterBuilder filterBuilder,
                                            SpatialPredicatesSpecification<?> spec,
                                            Map<String, Object> variables ) throws EntityFinderException
    {


        if (((SpatialPredicatesSpecification) spec).operator() instanceof SpatialConvertSpecification) {
            // return ((SpatialConvertSpecification) ((SpatialPredicatesSpecification) spec).operator()).convert(module);

            System.out.println("####### " + spec.operator().getClass().getSuperclass());

            if (ElasticSearchFinder.Mixin.EXTENDED_QUERY_EXPRESSIONS_CATALOG.get(spec.operator().getClass().getSuperclass()) != null) {

                ElasticSearchSpatialFinder.SpatialQuerySpecSupport spatialQuerySpecSupport = ElasticSearchFinder.Mixin.EXTENDED_QUERY_EXPRESSIONS_CATALOG.get(spec.operator().getClass().getSuperclass());
                spatialQuerySpecSupport.setModule(module, support);
                // return spatialQuerySpecSupport.processSpecification(filterBuilder, spec.operator(), variables);
                spatialQuerySpecSupport.processSpecification(filterBuilder, spec.operator(), variables);

            } else {
                throw new UnsupportedOperationException("Query specification unsupported by Elastic Search "
                        + "(New Query API support missing?): "
                        + spec.getClass() + ": " + spec);
            }
        }
    }



}
