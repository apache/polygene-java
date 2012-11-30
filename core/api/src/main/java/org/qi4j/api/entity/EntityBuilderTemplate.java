package org.qi4j.api.entity;

import org.qi4j.api.structure.Module;

/**
 * EntityBuilderTemplate.
 */
public abstract class EntityBuilderTemplate<T>
{
    Class<T> type;

    protected EntityBuilderTemplate( Class<T> type )
    {
        this.type = type;
    }

    protected abstract void build( T prototype );

    public T newInstance( Module module )
    {
        EntityBuilder<T> builder = module.currentUnitOfWork().newEntityBuilder( type );
        build( builder.instance() );
        return builder.newInstance();
    }
}
