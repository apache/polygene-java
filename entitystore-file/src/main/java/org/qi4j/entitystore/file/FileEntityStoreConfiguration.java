package org.qi4j.entitystore.file;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.Range;

/**
 * Configuration for the FileEntityStoreService
 */
public interface FileEntityStoreConfiguration
    extends ConfigurationComposite
{
    /**
     * The directory where the File Entity Store will be keep its persisted state.
     * <p>
     * Default: System.getProperty( "user.dir" ) + "/qi4j/filestore";
     * <br/>
     * Ignored if the FileConfiguration service is found.
     * </p>
     * <p>
     * The content inside this directory should not be modified directly, and doing so may corrupt the data.
     * </p>
     *
     * @return path to data file relative to current path
     */
    @Optional
    Property<String> directory();

    /** Defines how many slice directories the store should use.
     * <p>
     * Many operating systems run into performance problems when the number of files in a directory grows. If
     * you expect a large number of entities in the file entity store, it is wise to set the number of slices
     * (default is 1) to an approximation of the square root of number of expected entities.
     * </p>
     * <p>
     * For instance, if you estimate that you will have 1 million entities in the file entity store, you should
     * set the slices to 1000.
     * </p>
     * <p>
     * There is an limit of minimum 1 slice and maximum 10,000 slices, and if more slices than that is needed, you
     * are probably pushing this entitystore beyond its capabilities.
     * </p>
     * <p>
     * Note that the slices() can not be changed once it has been set, as it would cause the entity store not to
     * find the entities anymore.
     * </p>
     * @return the number of slices for the file entity store.
     */
    @Optional @Range(min=1, max=10000)
    Property<Integer> slices();
}
