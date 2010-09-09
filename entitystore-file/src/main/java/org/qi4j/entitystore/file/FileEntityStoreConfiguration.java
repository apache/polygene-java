package org.qi4j.entitystore.file;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Configuration for the FileEntityStoreService
 */
public interface FileEntityStoreConfiguration
    extends ConfigurationComposite
{
    /**
     * The directory where the File Entity Store will be keep its persisted state.
     *
     * Default: System.getProperty( "user.dir" ) + "/qi4j/filestore";
     *
     * @return path to data file relative to current path
     */
    @Optional
    Property<String> directory();
}
