package org.qi4j.library.geometry;

import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

public class TGeometryBuilder<T>
{
    @Structure
    private Module module;

    private ValueBuilder<T> builder;
    private T geometry;

    public TGeometryBuilder(Class<T> type)
    {
        builder = module.newValueBuilder( type );
        geometry = builder.prototype();
    }

    public T geometry()
    {
        return geometry;
    }

    public T newInstance()
    {
        return builder.newInstance();
    }
}
