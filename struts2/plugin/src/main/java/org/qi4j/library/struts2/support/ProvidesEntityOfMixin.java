package org.qi4j.library.struts2.support;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;

public abstract class ProvidesEntityOfMixin<T> implements ProvidesEntityOf<T>, StrutsAction {
    
    @This ProvidesEntityOf<T> entityProvider;
    @Structure UnitOfWorkFactory uowf;

    UnitOfWork uow;
    
    private String id;
    private T entity;

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public T getEntity() {
        return entity;
    }
    
    protected void loadEntity() {
        uow = uowf.currentUnitOfWork();
        try {
            entity = uow.find(entityProvider.getId(), typeToLoad());
        } catch (EntityCompositeNotFoundException e) {
            addActionError(getText("entity.not.found"));
        }
    }

    protected Class<T> typeToLoad() {
        return (Class<T>) findTypeVariables(entityProvider.getClass(), ProvidesEntityOf.class)[0];
    }
}