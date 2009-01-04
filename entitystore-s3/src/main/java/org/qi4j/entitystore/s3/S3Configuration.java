package org.qi4j.entitystore.s3;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;

/**
 * Configuration for the Amazon S3 EntityStore
 */
@Queryable( false )
public interface S3Configuration
    extends EntityComposite
{
    Property<String> accessKey();

    Property<String> secretKey();
}
