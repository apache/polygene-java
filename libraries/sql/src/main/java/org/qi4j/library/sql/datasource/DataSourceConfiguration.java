package org.qi4j.library.sql.datasource;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * Configuration for a DataSource pool.
 */
public interface DataSourceConfiguration
      extends ConfigurationComposite, Enabled
{
   Property<String> driver();

   Property<String> url();

   Property<String> username();

   Property<String> password();

   @UseDefaults
   Property<String> properties();
}
