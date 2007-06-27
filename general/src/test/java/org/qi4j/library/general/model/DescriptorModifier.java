package org.qi4j.library.general.model;

import org.qi4j.api.annotation.Modifies;

public class DescriptorModifier implements Descriptor
{
    @Modifies private Descriptor next;

    public String getDisplayValue()
    {
        return "My name is " + next.getDisplayValue();
    }
}
