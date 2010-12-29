package org.qi4j.entitystore.hazelcast;

import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * @author Paul Merlin <paul@nosphere.org>
 */
public interface HazelcastConfiguration
    extends ConfigurationComposite
{

    Property<String> mapName();
}
