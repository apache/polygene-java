package org.qi4j.entitystore.sql.database;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

public interface DatabaseConfiguration
        extends ConfigurationComposite
{

    Property<String> driver();

    Property<String> connectionURL();

    Property<String> dbName();

    @Optional
    Property<String> user();

    @Optional
    Property<String> password();

}
