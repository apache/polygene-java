package com.marcgrue.dcisample_a.infrastructure.dci;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * Methodful Role implementation base class
 *
 * Helps "inject" the Context object into the Role Player.
 */
public abstract class RoleMixin<T extends Context>
{
    public T context;

    // Other common role services/methods could be added here...

    @Structure
    public UnitOfWorkFactory uowf;

    @Structure
    public QueryBuilderFactory qbf;

    @Structure
    public ValueBuilderFactory vbf;

    /**
     * setContext is called with method invocation in {@link Context#setContext(Object, Context)}
     * (therefore "never used" according to IDE)
     */
    public void setContext( T context )
    {
        this.context = context;
    }
}
