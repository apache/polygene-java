package org.qi4j.entitystore.qrm;

import java.util.List;
import java.util.Properties;
import org.qi4j.api.property.Property;

/**
 * User: alex
 */
public interface QrmEntityStoreDescriptor
{

    List<Class> types();

    Properties props();
}
