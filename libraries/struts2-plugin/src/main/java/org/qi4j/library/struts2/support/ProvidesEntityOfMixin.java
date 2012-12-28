package org.qi4j.library.struts2.support;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesEntityOfMixin<T>
    implements ProvidesEntityOf<T>, StrutsAction
{

    @This
    private ProvidesEntityOf<T> entityProvider;

    @Structure
    private UnitOfWorkFactory uowf;

    private String id;
    private T entity;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId( String id )
    {
        this.id = id;
    }

    @Override
    public T getEntity()
    {
        return entity;
    }

    protected void loadEntity()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        try
        {
            entity = uow.get( typeToLoad(), entityProvider.getId() );
        }
        catch( NoSuchEntityException e )
        {
            addActionError( getText( "entity.not.found" ) );
        }
    }

    protected Class<T> typeToLoad()
    {
        return (Class<T>) findTypeVariables( entityProvider.getClass(), ProvidesEntityOf.class )[ 0 ];
    }
}