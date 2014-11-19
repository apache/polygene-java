package org.qi4j.index.elasticsearch.extensions.spatial.predicate;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinderSupport;
import org.qi4j.spi.query.EntityFinderException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jj on 19.11.14.
 */
public class ElasticSearchSpatialPredicateFinderSupport implements ElasticSearchSpatialFinderSupport.SpatialQuerySpecSupport {


    private static final Map<Class<?>, ElasticSearchSpatialPredicateFinderSupport.PredicateSpecification> SPATIAL_PREDICATE_OPERATIONS = new HashMap<>( 2 );


    public static interface PredicateSpecification extends ElasticSearchSpatialFinderSupport.ModuleHelper
    {
        void processSpecification(FilterBuilder filterBuilder, SpatialPredicatesSpecification<?>  spec, Map<String, Object> variables)  throws EntityFinderException;
    }

    static
    {
        ElasticSearchSpatialFinderSupport.SpatialQuerySpecSupport spatialQuerySpecSupport = new ElasticSearchSpatialFinderSupport.SpatialSupport();
        SPATIAL_PREDICATE_OPERATIONS.put(ST_WithinSpecification.class, new ST_WithinOperation());
        // SPATIAL_PREDICATE_OPERATIONS.put(SpatialConvertSpecification.class, spatialQuerySpecSupport);
    }


    Module module;

    public void setModule(Module module) {
        this.module = module;
    }


    public void processSpecification( FilterBuilder filterBuilder,
                                      // SpatialPredicatesSpecification<?>  spec,
                                      Specification<Composite> spec,
                                      Map<String, Object> variables )
            throws EntityFinderException
    {
        System.out.println("ElasticSearchSpatialPredicateFinderSupport::processSpecification() " + spec.getClass() );

        // LOGGER.trace( "Processing processSpatialPredicatesSpecification {}", spec );
        String name = ((SpatialPredicatesSpecification)spec).property().toString();
        String value = toString( ((SpatialPredicatesSpecification)spec).value(), variables );

        if( SPATIAL_PREDICATE_OPERATIONS.get( spec.getClass() ) != null ) {

            PredicateSpecification PredicateSpecification = SPATIAL_PREDICATE_OPERATIONS.get( spec.getClass());
            PredicateSpecification.setModule(module);
            PredicateSpecification.processSpecification(filterBuilder, (SpatialPredicatesSpecification)spec, variables);

        } else {
            throw new UnsupportedOperationException( "Spatial predicates specification unsupported by Elastic Search "
                    + "(New Query API support missing?): "
                    + spec.getClass() + ": " + spec );

        }
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
}


