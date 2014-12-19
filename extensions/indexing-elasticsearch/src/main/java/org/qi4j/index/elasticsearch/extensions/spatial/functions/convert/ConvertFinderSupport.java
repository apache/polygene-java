package org.qi4j.index.elasticsearch.extensions.spatial.functions.convert;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder;
import org.qi4j.spi.query.EntityFinderException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jj on 20.11.14.
 */
public class ConvertFinderSupport implements  ElasticSearchSpatialFinder.SpatialQuerySpecSupport {

    private static final Map<Class<?>, ConvertFinderSupport.ConvertSpecification> SPATIAL_CONVERT_OPERATIONS = new HashMap<>( 2 );

    static
    {
        SPATIAL_CONVERT_OPERATIONS.put(ST_GeomFromTextSpecification.class, new ST_GeometryFromText());
    }

    public static interface ConvertSpecification extends ElasticSearchSpatialFinder.ModuleHelper
    {
        void processSpecification(FilterBuilder filterBuilder, SpatialConvertSpecification<?> spec, Map<String, Object> variables)  throws EntityFinderException;
    }



    Module module;
    ElasticSearchSupport support;

    public void setModule(Module module, ElasticSearchSupport support) {

        this.module = module;
        this.support = support;
    }



    public void processSpecification( FilterBuilder filterBuilder,
                                      // SpatialPredicatesSpecification<?>  spec,
                                      Specification<?> spec,
                                      Map<String, Object> variables )
            throws EntityFinderException
    {
        System.out.println("ElasticSearchSpatialPredicateFinderSupport::processSpecification() " + spec.getClass() );


        if( SPATIAL_CONVERT_OPERATIONS.get( spec.getClass() ) != null ) {

            ConvertSpecification ConvertSpecification = SPATIAL_CONVERT_OPERATIONS.get( spec.getClass());
            ConvertSpecification.setModule(module, support);
            ConvertSpecification.processSpecification(filterBuilder, (SpatialConvertSpecification)spec, variables);

        } else {
            throw new UnsupportedOperationException( "Spatial predicates specification unsupported by Elastic Search "
                    + "(New Query API support missing?): "
                    + spec.getClass() + ": " + spec );

        }

    }

}
