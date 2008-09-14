package org.qi4j.library.struts2.support.add;

import static org.qi4j.library.struts2.util.ClassNames.classNameInDotNotation;
import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;

import com.opensymphony.xwork2.ActionSupport;

public abstract class ProvidesAddingOfMixin<T> extends ActionSupport implements ProvidesAddingOf<T> {

    @This ProvidesAddingOf<T> action;
    @Structure UnitOfWorkFactory uowf;

    UnitOfWork uow;
    EntityBuilder<T> builder;
    
    T entity;
    
    public T getState() {
        return builder.stateOfComposite();
    }

    public void prepare() throws Exception {
        prepareEntityBuilder();
    }
    
    public String input() {
        return INPUT;
    }
    
    @Override
    public String execute() throws Exception{
        entity = builder.newInstance();
        addSuccessMessage();
        return SUCCESS;
    }

    @SuppressWarnings("unchecked")
    protected Class<T> typeToAdd() {
        return (Class<T>) findTypeVariables(action.getClass(), ProvidesAddingOf.class)[0];
    }
    
    protected void addSuccessMessage() {
        addActionMessage(getText(classNameInDotNotation(typeToAdd()) + ".successfully.added"));
    }

    protected void prepareEntityBuilder() throws Exception {
        uow = uowf.currentUnitOfWork();
        builder = uow.newEntityBuilder(typeToAdd());
    }
}