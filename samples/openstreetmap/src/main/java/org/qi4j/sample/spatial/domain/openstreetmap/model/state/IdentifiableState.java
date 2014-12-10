package org.qi4j.sample.spatial.domain.openstreetmap.model.state;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;

/**
 * Created by jj on 28.11.14.
 */
public interface IdentifiableState {

    interface V1
    {
        @Optional
        Property<String> id();
    }

    public interface latest extends V1 {}
}
