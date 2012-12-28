package org.qi4j.api.value;

import org.qi4j.api.structure.Module;

/**
 * Builder template for Values.
 */
public abstract class ValueBuilderTemplate<T>
{
    Class<T> type;

    protected ValueBuilderTemplate( Class<T> type )
    {
        this.type = type;
    }

    protected abstract void build( T prototype );

    public T newInstance( Module module )
    {
        ValueBuilder<T> builder = module.newValueBuilder( type );
        build( builder.prototype() );
        return builder.newInstance();
    }
}
