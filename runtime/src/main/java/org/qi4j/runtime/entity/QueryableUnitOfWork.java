/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.entity;

import java.util.Map;
import org.qi4j.entity.EntityComposite;
import org.qi4j.query.QueryImpl;
import org.qi4j.query.Queryable;
import org.qi4j.query.QueryableIterable;

/**
 * Queryable implementation for UnitOfWorkInstance.
 */
public final class QueryableUnitOfWork
    implements Queryable
{
    UnitOfWorkInstance unit;

    public QueryableUnitOfWork( UnitOfWorkInstance unit )
    {
        this.unit = unit;
    }

    public <T> T find( QueryImpl<T> query )
    {
        Class resultType = query.getResultType();
        Map<String, EntityComposite> cache = unit.getEntityCache( resultType );

        QueryableIterable queryableCache = new QueryableIterable( cache.values() );

        return queryableCache.find( query );
    }

    public <T> Iterable<T> iterable( QueryImpl<T> query )
    {
        Class resultType = query.getResultType();
        Map<String, EntityComposite> cache = unit.getEntityCache( resultType );

        QueryableIterable queryableCache = new QueryableIterable( cache.values() );

        return queryableCache.iterable( query );
    }
}
