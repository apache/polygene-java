package org.qi4j.index.sql.support.skeletons;

import java.lang.reflect.AccessibleObject;

import org.qi4j.api.entity.Queryable;

/* package */ final class SQLSkeletonUtil
{

    /* package */ static boolean isQueryable( AccessibleObject accessor )
    {
        Queryable q = accessor.getAnnotation( Queryable.class );
        return q == null || q.value();
    }

    private SQLSkeletonUtil()
    {
    }

}
