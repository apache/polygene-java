/**
 * 
 */
package org.qi4j.library.struts2.support;

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

    protected abstract Class<T> typeToLoad();
}