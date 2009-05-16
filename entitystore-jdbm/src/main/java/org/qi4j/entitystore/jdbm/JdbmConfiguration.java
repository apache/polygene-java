package org.qi4j.entitystore.jdbm;

import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.common.UseDefaults;

/**
 * Configuration for the JdbmEntityStoreService
 */
public interface JdbmConfiguration
    extends ConfigurationComposite
{
    /** The file where the JDBM data will be stored
     *
     * @return path to data file relative to current path
     */
    Property<String> file();

    // JDBM RecordManager options
    @UseDefaults
    Property<Boolean> autoCommit();

    @UseDefaults
    Property<Boolean> disableTransactions();
}
