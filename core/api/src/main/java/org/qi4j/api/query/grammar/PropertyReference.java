package org.qi4j.api.query.grammar;

import java.util.function.Function;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
 * Property Reference.
 */
public interface PropertyReference
{
    <T> Function<Composite, Property<T>> reference();
}
