package org.qi4j.api.query.grammar.extensions.spatial.measurements;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.grammar.ExpressionSpecification;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.functional.Specification;

/**
 * Base Common Spatial Relationships Specification based on the DE-9IM
 */
public abstract class SpatialMeasurementsSpecification<T>
    extends ExpressionSpecification
{

    // TODO JJ : The following spatial functions are specified :
    // ST_Disjoint, ST_Intersects, ST_Touches, ST_Crosses, ST_Within,
    // ST_Contains and ST_Overlaps

    //  Known Specifications - http://en.wikipedia.org/wiki/Spatial_database
    // - SpatialMeasurementsSpecification
    // - SpatialFunctionsSpecification
    // - SpatialPredicatesSpecification
    // - SpatialConstructorsSpecification
    // - SpatialObserverFunctionsSpecification






    // protected final Iterable<Specification<Composite>> operands;

    protected final PropertyFunction<T> property;
    protected final T value;

    public SpatialMeasurementsSpecification(PropertyFunction<T> property, T value)
    {
        this.property = property;
        this.value = value;
    }

    public PropertyFunction<T> property()
    {
        return property;
    }

    public T value()
    {
        return value;
    }




    public Iterable<Specification<Composite>> operands()
    {
      //  return operands;
        return null;
    }
}
