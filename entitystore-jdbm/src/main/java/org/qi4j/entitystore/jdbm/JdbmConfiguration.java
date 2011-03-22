package org.qi4j.entitystore.jdbm;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Configuration for the JdbmEntityStoreService
 */
public interface JdbmConfiguration
        extends ConfigurationComposite
{
   /**
    * The file where the JDBM data will be stored
    * <p/>
    * Default: System.getProperty( "user.dir" ) + "/qi4j/jdbmstore.data";
    *
    * @return path to data file relative to current path
    */
   @Optional
   Property<String> file();

   // JDBM RecordManager options

   @UseDefaults
   Property<Boolean> autoCommit();

   @UseDefaults
   Property<Boolean> disableTransactions();
}
