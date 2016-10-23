package org.apache.zest.api.identity;

import java.util.UUID;

public class UuidGeneratorMixin
        implements IdentityGenerator
{
    @Override
    public Identity generate(Class<?> compositeType)
    {
        return StringIdentity.fromString(UUID.randomUUID().toString());
    }
}
