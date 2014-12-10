package org.qi4j.sample.spatial.domain.openstreetmap.model.state;

import org.qi4j.api.property.Property;

import java.util.Map;

/**
 * Created by jj on 28.11.14.
 */
public interface PropertiesState {

    interface V1
    {
        Property<Map<String, String>> properties();
    }

    public interface latest extends V1 {}
}
