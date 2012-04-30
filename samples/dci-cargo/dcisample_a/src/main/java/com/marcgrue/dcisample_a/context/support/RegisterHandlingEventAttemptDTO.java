package com.marcgrue.dcisample_a.context.support;

import com.marcgrue.dcisample_a.infrastructure.conversion.DTO;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import java.util.Date;

/**
 * The RegisterHandlingEventAttemptDTO simply helps move event registration data around.
 */
@Immutable
public interface RegisterHandlingEventAttemptDTO extends DTO
{
    @Optional
    Property<Date> registrationTime();

    @Optional
    Property<Date> completionTime();

    @Optional
    Property<String> trackingIdString();

    @Optional
    Property<String> eventTypeString();

    @Optional
    Property<String> unLocodeString();

    @Optional
    Property<String> voyageNumberString();
}
