package org.qi4j.library.general.model;

import org.qi4j.api.annotation.Uses;

public final class DescriptorMixin implements Descriptor
{
    @Uses private Name name;

    public String getDisplayValue()
    {
        return name.getName();
    }
}
