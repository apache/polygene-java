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
package org.qi4j.library.struts2.support.list;

import com.opensymphony.xwork2.ActionSupport;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesListOfMixin<T>
    extends ActionSupport
    implements ProvidesListOf<T>
{

    @This
    private ProvidesListOf<T> action;

    @Structure
    private UnitOfWorkFactory uowf;

    @Structure
    private QueryBuilderFactory qbf;

    private Iterable<T> results;

    @Override
    public Iterable<T> list()
    {
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
    @Override
    public void prepare()
        throws Exception
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        QueryBuilder<T> qb = qbf.newQueryBuilder( typeToList() );
        results = uow.newQuery( qb );
    }

    private Class<T> typeToList()
    {
        return (Class<T>) findTypeVariables( action.getClass(), ProvidesListOf.class )[ 0 ];
    }
}