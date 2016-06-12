/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_a.infrastructure.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;

/**
 * Callback Wicket model that holds a Zest Query object that can be called when needed to
 * retrieve fresh data.
 */
public abstract class QueryModel<T extends Identity>
    extends ReadOnlyModel<List<T>>
{
    private Class<T> dtoClass;
    private transient List<T> dtoList;

    @Structure
    private UnitOfWorkFactory uowf;

    public QueryModel( Class<T> dtoClass )
    {
        this.dtoClass = dtoClass;
    }

    public List<T> getObject()
    {
        if( dtoList != null )
        {
            return dtoList;
        }

        dtoList = new ArrayList<>();
        for( T entity : getQuery() )
        {
            dtoList.add( getValue( entity ) );
        }

        return dtoList;
    }

    // Callback to retrieve the (unserializable) Zest Query object
    public abstract Query<T> getQuery();

    public T getValue( T entity )
    {
        return uowf.currentUnitOfWork().toValue( dtoClass, entity );
    }

    public void detach()
    {
        dtoList = null;
    }
}