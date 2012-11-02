package org.qi4j.library.struts2.support.view;

import org.qi4j.api.injection.scope.This;
import org.qi4j.library.struts2.support.ProvidesEntityOfMixin;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesViewOfMixin<T>
    extends ProvidesEntityOfMixin<T>
    implements ProvidesViewOf<T>
{
    @This
    private ProvidesViewOf<T> action;

    @Override
    public String execute()
    {
        loadEntity();
        return SUCCESS;
    }

    @Override
    protected Class<T> typeToLoad()
    {
        return (Class<T>) findTypeVariables( action.getClass(), ProvidesViewOf.class )[ 0 ];
    }
}