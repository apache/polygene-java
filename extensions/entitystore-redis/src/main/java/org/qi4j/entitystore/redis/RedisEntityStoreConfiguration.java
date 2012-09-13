package org.qi4j.entitystore.redis;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

// START SNIPPET: config
public interface RedisEntityStoreConfiguration
        extends ConfigurationComposite
{

    /**
     * Redis host.
     *
     * Defaulted to 127.0.0.1.
     */
    @Optional
    Property<String> host();

    /**
     * Redis port.
     *
     * Defaulted to 6379.
     */
    @Optional
    Property<Integer> port();

    /**
     * Connection timeout in milliseconds.
     *
     * Defaulted to 2000.
     */
    @Optional
    Property<Integer> timeout();

    /**
     * Redis password.
     */
    @Optional
    Property<String> password();

    /**
     * Redis database.
     *
     * Defaulted to 0.
     */
    @Optional
    Property<Integer> database();

}
// END SNIPPET: config
