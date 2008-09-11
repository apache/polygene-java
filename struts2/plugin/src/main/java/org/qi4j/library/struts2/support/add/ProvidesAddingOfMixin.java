/**
 * 
 */
package org.qi4j.library.struts2.support.add;

import static org.qi4j.library.struts2.util.ClassUtil.classNameInDotNotation;

import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;

import com.opensymphony.xwork2.ActionSupport;

public abstract class ProvidesAddingOfMixin<T> extends ActionSupport implements ProvidesAddingOf<T> {

    @This Composite action;
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
        if (action.type().getAnnotation(Adds.class) == null) {
            throw new AddsAnnotationMissingException(action.type());
        }
        return (Class<T>) action.type().getAnnotation(Adds.class).value();
    }
    
    protected void addSuccessMessage() {
        addActionMessage(getText(classNameInDotNotation(typeToAdd()) + ".successfully.added"));
    }

    protected void prepareEntityBuilder() throws Exception {
        uow = uowf.currentUnitOfWork();
        builder = uow.newEntityBuilder(typeToAdd());
    }
}