package org.qi4j.entitystore.qrm;

import java.util.Properties;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.common.Optional;

/**
 * User: alex
 */
public interface ConnectionConfig extends ConfigurationComposite
{

    Property<String> dbUrl();

    Property<String> schemaUrl();

    Property<String> dataUrl();

    @Optional Property<Properties> connectionProperties();

}
