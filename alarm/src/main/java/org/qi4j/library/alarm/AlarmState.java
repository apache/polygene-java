package org.qi4j.library.alarm;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

import java.util.Map;

public interface AlarmState
{
    @AlarmName
    Property<String> systemName();

    @Optional
    Property<String> description();

    @UseDefaults
    Property<Map<String,String>> attributes();

    Property<AlarmStatus> currentStatus();
}
