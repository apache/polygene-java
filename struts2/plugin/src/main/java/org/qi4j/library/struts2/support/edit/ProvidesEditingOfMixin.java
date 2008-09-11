/**
 * 
 */
package org.qi4j.library.struts2.support.edit;

import org.qi4j.composite.Composite;
import org.qi4j.injection.scope.This;
import org.qi4j.library.struts2.support.ProvidesEntityOfMixin;


public abstract class ProvidesEditingOfMixin<T> extends ProvidesEntityOfMixin<T> implements ProvidesEditingOf<T> {

    @This Composite action;
    
    public void prepare() throws Exception {
        loadEntity();
    }

    public void prepareInput() throws Exception {
        
    }

    public String input() {
        if (getEntity() == null) {
            return "entity-not-found";
        }
        return INPUT;
    }
    
    @Override
    protected Class<T> typeToLoad() {
        if (action.type().getAnnotation(Edits.class) == null) {
            throw new EditsAnnotationMissingException(action.type());
        }
        return (Class<T>) action.type().getAnnotation(Edits.class).value();
    }
}