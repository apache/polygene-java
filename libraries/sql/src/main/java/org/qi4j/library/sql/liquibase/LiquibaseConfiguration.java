package org.qi4j.library.sql.liquibase;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * Configuration for Liquibase
 */
public interface LiquibaseConfiguration
      extends ConfigurationComposite, Enabled
{
   @UseDefaults
   Property<String> contexts();

   @UseDefaults
   Property<String> changeLog();
}