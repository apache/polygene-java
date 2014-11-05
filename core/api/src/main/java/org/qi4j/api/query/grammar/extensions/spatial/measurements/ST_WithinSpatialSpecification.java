package org.qi4j.api.query.grammar.extensions.spatial.measurements;

import org.qi4j.api.query.grammar.PropertyFunction;

import java.util.Collection;

/**
 * Regular expression match Specification.
 */
public class ST_WithinSpatialSpecification<T>
        extends SpatialMeasurementsSpecification
{



    // public ST_WithinSpatialSpecification(Property<T> property, T value, Double range)
    // PropertyFunction<T> property
    public ST_WithinSpatialSpecification(PropertyFunction<T> property, T value, Double range)
    {
        // super( property, value );
        // super(T);
        super (property, value);
    }

    private PropertyFunction<? extends Collection<T>> collectionProperty;
    private Iterable<T> valueCollection;

//    public WithInSpatialSpecification( PropertyFunction<? extends Collection<T>> collectionProperty,
//                                     Iterable<T> valueCollection
//    )
//    {
//        this.collectionProperty = collectionProperty;
//        this.valueCollection = valueCollection;
//    }

    public PropertyFunction<? extends Collection<T>> collectionProperty()
    {
        return collectionProperty;
    }

    public Iterable<T> containedValues()
    {
        return valueCollection;
    }

    // @Override
    public boolean satisfiedBy( Object item )
    {
      return true;
    }

//    @Override
//    public boolean satisfiedBy( Composite item )
//    {
//        Collection<T> collection = collectionProperty.map( item ).get();
//
//        if( collection == null )
//        {
//            return false;
//        }
//
//        for( T value : valueCollection )
//        {
//            if( !collection.contains( value ) )
//            {
//                return false;
//            }
//        }
//
//        return true;
//    }

    @Override
    public String toString()
    {
        return "WithInSpatialSpecification()"; // collectionProperty + " contains " + Iterables.toList(valueCollection);
    }
}
