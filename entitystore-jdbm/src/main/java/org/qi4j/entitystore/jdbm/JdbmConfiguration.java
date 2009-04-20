package org.qi4j.entitystore.jdbm;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;

/**
 * JAVADOC
 */
@Queryable( false )
public interface JdbmConfiguration
    extends ConfigurationComposite
{
    Property<String> file();

    /**
     * If no Serializable classes uses the readObject()/writeObject() override for
     * serialization, it is possible to enable the turboMode.
     *
     * <b>Note:</b> Set only to true, if you are convinced nothing uses or will use the Serialization override methods.
     *
     * @return the turboMode setting. True if turboMode is enabled, but serialization of entities that contain classes
     *         that has readObject() or writeObject() overrides will fail.
     */
    @UseDefaults Property<Boolean> turboMode();
}
