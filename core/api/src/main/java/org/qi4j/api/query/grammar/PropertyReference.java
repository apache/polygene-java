package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.functional.Function;

/**
 * Property Reference.
 */
public interface PropertyReference
{
    <T> Function<Composite, Property<T>> reference();
}
