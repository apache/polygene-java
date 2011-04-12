package org.qi4j.library.alarm;

import java.util.Map;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

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
