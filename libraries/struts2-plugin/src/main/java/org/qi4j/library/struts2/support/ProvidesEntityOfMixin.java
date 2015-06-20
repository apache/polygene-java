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
package org.qi4j.library.struts2.support;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesEntityOfMixin<T>
    implements ProvidesEntityOf<T>, StrutsAction
{

    @This
    private ProvidesEntityOf<T> entityProvider;

    @Structure
    private UnitOfWorkFactory uowf;

    private String id;
    private T entity;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId( String id )
    {
        this.id = id;
    }

    @Override
    public T getEntity()
    {
        return entity;
    }

    protected void loadEntity()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        try
        {
            entity = uow.get( typeToLoad(), entityProvider.getId() );
        }
        catch( NoSuchEntityException e )
        {
            addActionError( getText( "entity.not.found" ) );
        }
    }

    protected Class<T> typeToLoad()
    {
        return (Class<T>) findTypeVariables( entityProvider.getClass(), ProvidesEntityOf.class )[ 0 ];
    }
}