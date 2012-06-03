package org.qi4j.samples.forum.domainevent;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * TODO
 */
public interface ParameterValue
    extends ValueComposite
{
    Property<String> name();

    Property<Object> value();
}
