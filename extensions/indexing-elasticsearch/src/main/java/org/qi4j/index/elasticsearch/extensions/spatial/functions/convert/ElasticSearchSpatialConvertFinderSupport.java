package org.qi4j.index.elasticsearch.extensions.spatial.functions.convert;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinderSupport;
import org.qi4j.spi.query.EntityFinderException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jj on 20.11.14.
 */
public class ElasticSearchSpatialConvertFinderSupport implements  ElasticSearchSpatialFinderSupport.SpatialQuerySpecSupport {

    private static final Map<Class<?>, ElasticSearchSpatialConvertFinderSupport.ConvertSpecification> SPATIAL_CONVERT_OPERATIONS = new HashMap<>( 2 );


    public static interface ConvertSpecification extends ElasticSearchSpatialFinderSupport.ModuleHelper
    {

        TGeometry processSpecification(FilterBuilder filterBuilder, SpatialConvertSpecification<?> spec, Map<String, Object> variables)  throws EntityFinderException;
    }

    static
    {
        SPATIAL_CONVERT_OPERATIONS.put(ST_GeomFromTextSpecification.class, new ST_GeometryFromTextFunction());
    }

    Module module;

    public void setModule(Module module) {
        this.module = module;
    }



    public TGeometry processSpecification( FilterBuilder filterBuilder,
                                      // SpatialPredicatesSpecification<?>  spec,
                                      Specification<?> spec,
                                      Map<String, Object> variables )
            throws EntityFinderException
    {
        System.out.println("ElasticSearchSpatialPredicateFinderSupport::processSpecification() " + spec.getClass() );


        if( SPATIAL_CONVERT_OPERATIONS.get( spec.getClass() ) != null ) {

            ConvertSpecification ConvertSpecification = SPATIAL_CONVERT_OPERATIONS.get( spec.getClass());
            ConvertSpecification.setModule(module);
            return ConvertSpecification.processSpecification(filterBuilder, (SpatialConvertSpecification)spec, variables);

        } else {
            throw new UnsupportedOperationException( "Spatial predicates specification unsupported by Elastic Search "
                    + "(New Query API support missing?): "
                    + spec.getClass() + ": " + spec );

        }
        // return null;
    }

}
