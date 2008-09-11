/**
 * 
 */
package org.qi4j.library.struts2.support.view;

import org.qi4j.composite.Composite;
import org.qi4j.injection.scope.This;
import org.qi4j.library.struts2.support.ProvidesEntityOfMixin;
import org.qi4j.library.struts2.support.edit.EditsAnnotationMissingException;


public abstract class ProvidesViewOfMixin<T> extends ProvidesEntityOfMixin<T> implements ProvidesViewOf<T> {

    @This Composite action;

    public String execute() {
        loadEntity();
        return SUCCESS;
    }
    
    protected Class<T> typeToLoad() {
        if (action.type().getAnnotation(ViewOf.class) == null) {
            throw new EditsAnnotationMissingException(action.type());
        }
        return (Class<T>) action.type().getAnnotation(ViewOf.class).value();
    }
}