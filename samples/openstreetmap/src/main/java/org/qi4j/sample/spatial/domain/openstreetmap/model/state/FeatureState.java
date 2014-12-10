package org.qi4j.sample.spatial.domain.openstreetmap.model.state;

import org.qi4j.api.association.Association;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.Property;

/**
 * Created by jj on 28.11.14.
 */
public interface FeatureState {

    interface V1
    {
       //  Association<PropertiesState> properties();
        Property<TGeometry> feature();
    }

    public interface latest extends V1 {}
}
