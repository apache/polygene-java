package org.qi4j.samples.forum.domainevent;

import java.util.List;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * TODO
 */
public interface DomainEventValue
    extends ValueComposite
{
    // Version of the application that created these events
    Property<String> version();

    // When the event occurred
    Property<Long> timestamp();

    // Selected objects
    @UseDefaults
    Property<List<String>> selection();

    // Type of the entity being invoked
    Property<String> context();

    // Name of method/event
    Property<String> name();

    // Method parameters as JSON
    @UseDefaults
    Property<List<ParameterValue>> parameters();
}
