package com.marcgrue.dcisample_b.infrastructure.dci;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * RoleMixin
 *
 * Base class for Methodful Roles in Contexts
 *
 * Helps "inject" the Context object into the Role Player.
 *
 * The context field name is now "c" since we don't want the word "context" interfere
 * with domain semantics. We could for instance in one domain have some "material context"
 * where "context" is not a DCI Context but the context of some material. In that case a
 * DCI keyword "context" would confuse the semantics. We could also use "ctx", but why not
 * the shortest option? Less typing, and we know what it is. Furthermore a single letter is
 * easier to spot in the code masses than "ctx" (good to be able to quickly spot all uses
 * of the Context).
 */
public abstract class RoleMixin<T extends Context>
{
    // Context object reference
    public T c;

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
        c = context;
    }
}
