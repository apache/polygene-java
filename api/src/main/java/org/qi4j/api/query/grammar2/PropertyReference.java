package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Function;

/**
* TODO
*/
public interface PropertyReference
{
    <T> Function<Composite, Property<T>> reference();
}
