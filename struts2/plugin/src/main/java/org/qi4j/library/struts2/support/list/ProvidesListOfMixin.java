package org.qi4j.library.struts2.support.list;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;

import com.opensymphony.xwork2.ActionSupport;

public abstract class ProvidesListOfMixin<T> extends ActionSupport implements ProvidesListOf<T> {

    @This ProvidesListOf<T> action;
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
        return (Class<T>) findTypeVariables(action.getClass(), ProvidesListOf.class)[0];
    }
}