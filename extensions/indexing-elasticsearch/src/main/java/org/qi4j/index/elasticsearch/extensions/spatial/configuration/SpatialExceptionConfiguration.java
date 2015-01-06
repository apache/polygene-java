package org.qi4j.index.elasticsearch.extensions.spatial.configuration;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Created by jj on 25.12.14.
 */
public interface SpatialExceptionConfiguration extends ConfigurationComposite
{

    @Optional
    Property<Integer> index();
}
