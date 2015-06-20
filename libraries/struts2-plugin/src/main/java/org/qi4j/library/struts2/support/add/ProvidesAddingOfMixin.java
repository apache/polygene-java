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
package org.qi4j.library.struts2.support.add;

import com.opensymphony.xwork2.ActionSupport;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import static org.qi4j.library.struts2.util.ClassNames.classNameInDotNotation;
import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesAddingOfMixin<T>
    extends ActionSupport
    implements ProvidesAddingOf<T>
{

    @This
    private ProvidesAddingOf<T> action;

    @Structure
    private UnitOfWorkFactory uowf;

    private EntityBuilder<T> builder;

    @Override
    public T getState()
    {
        return builder.instance();
    }

    @Override
    public void prepare()
        throws Exception
    {
        prepareEntityBuilder();
    }

    @Override
    public String input()
    {
        return INPUT;
    }

    @Override
    public String execute()
        throws Exception
    {
        addSuccessMessage();
        return SUCCESS;
    }

    @SuppressWarnings( "unchecked" )
    protected Class<T> typeToAdd()
    {
        return (Class<T>) findTypeVariables( action.getClass(), ProvidesAddingOf.class )[ 0 ];
    }

    protected void addSuccessMessage()
    {
        addActionMessage( getText( classNameInDotNotation( typeToAdd() ) + ".successfully.added" ) );
    }

    protected void prepareEntityBuilder()
        throws Exception
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        builder = uow.newEntityBuilder( typeToAdd() );
    }
}