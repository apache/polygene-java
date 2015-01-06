package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_IntersectsSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder;
import org.qi4j.spi.query.EntityFinderException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jj on 19.11.14.
 */
public class PredicateFinderSupport implements ElasticSearchSpatialFinder.SpatialQuerySpecSupport {


    private static final Map<Class<?>, PredicateFinderSupport.PredicateSpecification> SPATIAL_PREDICATE_OPERATIONS = new HashMap<>( 3 );


    public static interface PredicateSpecification extends ElasticSearchSpatialFinder.ModuleHelper
    {
        void processSpecification(FilterBuilder filterBuilder, SpatialPredicatesSpecification<?>  spec, Map<String, Object> variables)  throws EntityFinderException;
    }

    static
    {
        SPATIAL_PREDICATE_OPERATIONS.put(ST_WithinSpecification.class, new ST_Within());
        SPATIAL_PREDICATE_OPERATIONS.put(ST_DisjointSpecification.class, new ST_DisjointV3());
        SPATIAL_PREDICATE_OPERATIONS.put(ST_IntersectsSpecification.class, new ST_Intersects());
    }


    Module module;
    ElasticSearchSupport support;

    public void setModule(Module module, ElasticSearchSupport support)
    {
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


        if( SPATIAL_PREDICATE_OPERATIONS.get( spec.getClass() ) != null ) {

            PredicateSpecification PredicateSpecification = SPATIAL_PREDICATE_OPERATIONS.get( spec.getClass());
            PredicateSpecification.setModule(module, support);
            PredicateSpecification.processSpecification(filterBuilder, (SpatialPredicatesSpecification)spec, variables);

        } else {
            throw new UnsupportedOperationException( "Spatial predicates specification unsupported by Elastic Search "
                    + "(New Query API support missing?): "
                    + spec.getClass() + ": " + spec );

        }
    }

}


