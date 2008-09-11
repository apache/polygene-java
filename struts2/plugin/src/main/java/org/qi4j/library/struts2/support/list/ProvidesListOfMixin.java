/**
 * 
 */
package org.qi4j.library.struts2.support.list;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.qi4j.composite.Composite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;

import com.opensymphony.xwork2.ActionSupport;

public abstract class ProvidesListOfMixin<T> extends ActionSupport implements ProvidesListOf<T> {

    @This Composite action;
    @Structure UnitOfWorkFactory uowf;
    
    Iterable<T> results;
    
    public Iterable<T> list() {
        return results;
    }
    
    /**
     * This is where we'll load the list of entities.  Not because it is any better than the execute() method, I
     * would actually prefer doing it in the execute method.  But since these list actions can be the target
     * of redirects after errors, actionErrors may not be empty which would mean execute() would never get called.
     * One way around this would be to have a different interceptor stack just for these list actions that doesn't
     * do validation, but for now we'll just use the prepare() method.  We can change it easily enough later if this
     * becomes an issue for some reason.
     */
    public void prepare() throws Exception {
        UnitOfWork uow = uowf.currentUnitOfWork();
        QueryBuilderFactory qbf = uow.queryBuilderFactory();
        QueryBuilder<T> qb = qbf.newQueryBuilder(typeToList());
        results = qb.newQuery();
    }
            
    private Class<T> typeToList() {
        if (action.type().getAnnotation(ListOf.class) == null) {
            throw new ListOfAnnotationMissingException(action.type());
        }
        return (Class<T>) action.type().getAnnotation(ListOf.class).value();
    }
}