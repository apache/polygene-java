package org.apache.polygene.spi.type;

import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;

public interface ValueTypeFactory
{
    ValueType valueTypeOf( ModuleDescriptor module, Object object );

    ValueType valueTypeOf( ModuleDescriptor module, Class<?> type );
}
