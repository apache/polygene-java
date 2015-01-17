package org.qi4j.api.query.grammar.extensions.spatial.predicate;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.ExpressionSpecification;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.functional.Specification;


public abstract class SpatialPredicatesSpecification<T extends TGeometry>
        extends ExpressionSpecification
{
    protected final PropertyFunction<T> property;
    protected final TGeometry param;
    protected final Specification<SpatialConvertSpecification> operator;

    public SpatialPredicatesSpecification(PropertyFunction<T> property, TGeometry param)
    {
        this.property = property;
        this.param = param;
        this.operator = null;
    }

    public SpatialPredicatesSpecification(PropertyFunction<T> property, Specification<SpatialConvertSpecification> operator)
    {
        this.property = property;
        this.operator = operator;
        this.param = null;
    }

    public PropertyFunction<T> property()
    {
        return property;
    }

    @Override
    public final boolean satisfiedBy(Composite item)
    {
        try
        {
            Property<T> prop = property.map(item);

            if (prop == null)
            {
                return false;
            }

            TGeometry propValue = prop.get();
            if (propValue == null)
            {
                return false;
            }

            return compare(propValue);
        } catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    protected abstract boolean compare(TGeometry value);

    public TGeometry param()
    {
        return param;
    }

    public Specification<SpatialConvertSpecification> operator()
    {
        return operator;
    }
}
