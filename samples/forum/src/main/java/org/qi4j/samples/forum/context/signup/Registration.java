package org.qi4j.samples.forum.context.signup;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * TODO
 */
public interface Registration
    extends ValueComposite
{
    Property<String> name();

    Property<String> realName();

    Property<String> email();

    Property<String> password();
}
