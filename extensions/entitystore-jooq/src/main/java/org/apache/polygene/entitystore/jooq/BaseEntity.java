package org.apache.polygene.entitystore.jooq;

import java.time.Instant;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.identity.Identity;

class BaseEntity
{
    EntityDescriptor type;
    Identity identity;
    String version;
    String applicationVersion;
    Instant modifedAt;
    Instant createdAt;
    Identity currentValueIdentity;
}
