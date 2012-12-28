package org.qi4j.index.sql.support.skeletons;

import java.lang.reflect.AccessibleObject;
import org.qi4j.api.entity.Queryable;

/* package */final class SQLSkeletonUtil
{

    /* package */static boolean isQueryable( AccessibleObject accessor )
    {
        Queryable q = accessor.getAnnotation( Queryable.class );
        return q == null || q.value();
    }

    private SQLSkeletonUtil()
    {
    }

    /**
     * Required for Lazy.
     *
     * @author Stanislav Muhametsin
     *
     * @param <T> The result variable type.
     */
    public static interface LazyInit<T, TException extends Throwable>
    {
        T create() throws TException;
    }

    /**
     * Non-threadsafe implementation of C#'s Lazy&lt;T&gt;. I wonder if Java has something like this
     * already done?
     *
     * @author Stanislav Muhametsin
     *
     * @param <T> The result variable type.
     */
    public static final class Lazy<T, TException extends Throwable>
    {
        private final LazyInit<T, TException> m_init;
        private T m_cachedValue;

        public Lazy( LazyInit<T, TException> init )
        {
            this.m_init = init;
            this.m_cachedValue = null;
        }

        public T getValue() throws TException
        {
            if( this.m_cachedValue == null )
            {
                this.m_cachedValue = this.m_init.create();
            }
            return this.m_cachedValue;
        }

        public boolean hasValue()
        {
            return this.m_cachedValue != null;
        }
    }

    public static final class Reference<T>
    {
        private T m_reference;

        public void setReference( T reference )
        {
            this.m_reference = reference;
        }

        public T getReference()
        {
            return this.m_reference;
        }
    }

}
