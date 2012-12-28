package org.qi4j.library.struts2.support.add;

import com.opensymphony.xwork2.ActionSupport;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import static org.qi4j.library.struts2.util.ClassNames.classNameInDotNotation;
import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesAddingOfMixin<T>
    extends ActionSupport
    implements ProvidesAddingOf<T>
{

    @This
    private ProvidesAddingOf<T> action;

    @Structure
    private UnitOfWorkFactory uowf;

    private EntityBuilder<T> builder;

    @Override
    public T getState()
    {
        return builder.instance();
    }

    @Override
    public void prepare()
        throws Exception
    {
        prepareEntityBuilder();
    }

    @Override
    public String input()
    {
        return INPUT;
    }

    @Override
    public String execute()
        throws Exception
    {
        addSuccessMessage();
        return SUCCESS;
    }

    @SuppressWarnings( "unchecked" )
    protected Class<T> typeToAdd()
    {
        return (Class<T>) findTypeVariables( action.getClass(), ProvidesAddingOf.class )[ 0 ];
    }

    protected void addSuccessMessage()
    {
        addActionMessage( getText( classNameInDotNotation( typeToAdd() ) + ".successfully.added" ) );
    }

    protected void prepareEntityBuilder()
        throws Exception
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        builder = uow.newEntityBuilder( typeToAdd() );
    }
}