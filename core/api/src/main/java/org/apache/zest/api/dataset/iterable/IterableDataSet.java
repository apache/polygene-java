/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.api.dataset.iterable;

import java.util.function.Function;
import org.apache.zest.api.dataset.DataSet;
import org.apache.zest.api.dataset.Query;
import org.apache.zest.functional.Iterables;
import org.apache.zest.functional.Specification;

/**
 * TODO
 */
public class IterableDataSet<T>
    implements DataSet<T>
{
    private Iterable<T> iterable;

    public IterableDataSet( Iterable<T> iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public DataSet<T> constrain( Specification<T> selection )
    {
        return new IterableDataSet<T>( Iterables.filter( selection, iterable ) );
    }

    @Override
    public <U> DataSet<U> project( Function<T, U> conversion )
    {
        return new IterableDataSet<U>( Iterables.map( conversion, iterable ) );
    }

    @Override
    public Query<T> newQuery()
    {
        return new IterableQuery<T>( iterable );
    }
}
