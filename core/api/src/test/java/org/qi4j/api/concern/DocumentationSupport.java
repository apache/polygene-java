package org.qi4j.api.concern;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.injection.InjectionScope;

public class DocumentationSupport
{
// START SNIPPET: class
    @AppliesTo( java.sql.Connection.class )
    public class CacheConcern extends GenericConcern
        implements InvocationHandler
    {
// END SNIPPET: class
        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return null;
        }
    }

// START SNIPPET: filter
    @AppliesTo( BusinessAppliesToFilter.class )
    public class BusinessConcern extends GenericConcern
        implements InvocationHandler
    {
// END SNIPPET: filter
        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return null;
        }
    }

// START SNIPPET: filter
    public class BusinessAppliesToFilter
        implements AppliesToFilter
    {

        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass
        )
        {
            return true; // Some criteria for when a method is wrapped with the concern.
        }
    }
// END SNIPPET: filter


// START SNIPPET: annotation
    @AppliesTo( Audited.class )
    public class AuditConcern extends GenericConcern
        implements InvocationHandler
    {
// START SNIPPET: annotation
        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return null;
        }
    }

// START SNIPPET: annotation
    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD } )
    @Documented
    @InjectionScope
    public @interface Audited
    {
    }
// END SNIPPET: annotation
}
