package org.qi4j.entity.s3;

import org.qi4j.entity.EntityComposite;
import org.qi4j.property.Property;

/**
 * Configuration for the Amazon S3 EntityStore
 */
public interface S3Configuration
    extends EntityComposite
{
    Property<String> accessKey();

    Property<String> secretKey();
}
